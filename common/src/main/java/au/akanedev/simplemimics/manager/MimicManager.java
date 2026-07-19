package au.akanedev.simplemimics.manager;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.entity.MimicEntity;
import au.akanedev.simplemimics.registry.ModEntities;
import au.akanedev.simplemimics.voice.VoiceHandler;
import au.akanedev.simplemimics.registry.ConfigRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages mimic spawning, AI behavior, and lifecycle
 */
public class MimicManager {

    private static MimicManager INSTANCE;

    // Active mimics: mimic UUID -> mimic entity
    private final Map<UUID, MimicEntity> activeMimics = new ConcurrentHashMap<>();

    // Tracking when each mimic was spawned (for lifetime management)
    private final Map<UUID, Long> spawnTimes = new ConcurrentHashMap<>();

    // Config
    private static final int MAX_MIMICS_PER_PLAYER = 1;
    private static final int MAX_TOTAL_MIMICS = 3;
    private static final long MIMIC_LIFETIME_MS = 8 * 60 * 1000; // 8 minutes
    private static final long RESPAWN_INTERVAL_MS = 2 * 60 * 1000; // Check every 2 minutes
    private static float CHANCE_TO_SPEAK = (float) ConfigRegistry.get("CHANCE_TO_SPEAK").get();

    private long lastRespawnCheck = 0;

    // Follow distance for Target B
    private static final double STALK_DISTANCE = 8.0;
    private static final double STALK_TOO_CLOSE = 3.0;
    private static final double STALK_TOO_FAR = 15.0;

    // Minimum distance to stay hidden from Target B
    private static final double MIN_HIDDEN_DISTANCE = 4.0;
    private static final double MAX_HIDDEN_DISTANCE = 5.0;

    private Random random = new Random();

    private MimicManager() {}

    public static MimicManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MimicManager();
        }
        return INSTANCE;
    }

    /**
     * Called every server tick to update mimic behavior
     */
    public void onServerTick(MinecraftServer server) {
        if (server == null || server.getPlayerList() == null) return;
        server.getPlayerList().getPlayers().forEach(player -> {
            Level level = player.level();
            long currentTime = System.currentTimeMillis();
            CHANCE_TO_SPEAK = (float) ConfigRegistry.get("CHANCE_TO_SPEAK").get();
            // Check for respawns
            if (currentTime - lastRespawnCheck > RESPAWN_INTERVAL_MS) {
                lastRespawnCheck = currentTime;
                tryRespawnMimics(level);
            }

            // Update existing mimics
            List<UUID> mimicsToRemove = new ArrayList<>();

            for (Map.Entry<UUID, MimicEntity> entry : activeMimics.entrySet()) {
                UUID mimicId = entry.getKey();
                MimicEntity mimic = entry.getValue();

                // Check lifetime
                Long spawnTime = spawnTimes.get(mimicId);
                if (spawnTime != null && currentTime - spawnTime > MIMIC_LIFETIME_MS) {
                    mimicsToRemove.add(mimicId);
                    continue;
                }

                // Check if Target A is still online
                ServerPlayer targetA = (ServerPlayer) mimic.getTargetA();
                if (targetA == null || !targetA.isAlive()) {
                    // Target A gone - remove mimic
                    mimicsToRemove.add(mimicId);
                    continue;
                }
                String name = targetA.getGameProfile().getName();

                // Name Check
                if (!mimic.getName().getString().equals(name)) {
                    mimic.setCustomName(Component.literal(name));
                }

                // Update behavior
                updateMimicBehavior(mimic, targetA);
            }

            // Remove dead mimics
            for (UUID id : mimicsToRemove) {
                removeMimic(id);
            }
        });

    }



    /**
     * Update a single mimic's AI behaviour.
     */
    private void updateMimicBehavior(MimicEntity mimic, ServerPlayer targetA) {
        if (mimic == null || targetA == null) return;
        if (!mimic.isAlive()) {
            UUID MimicUUID = mimic.getUUID();
            activeMimics.remove(MimicUUID);
            return;
        };

        Level level = mimic.level();
        if (level == null) return;

        // =========================
        // TARGET B HANDLING
        // =========================

        ServerPlayer targetB = (ServerPlayer) mimic.getTargetB();

        if (targetB == null || !targetB.isAlive()) {

            if (random.nextFloat() < 0.7f) {
                targetB = findNearbyPlayer(mimic, targetA);
            } else {
                targetB = targetA;
            }

            if (targetB != null) {
                mimic.setTargetBUUID(targetB.getUUID().toString());
            }

            return;
        }

        // =========================
        // DISTANCE CHECK
        // =========================

        double distance = mimic.position().distanceTo(targetB.position());

        // =========================
        // MOVEMENT LOGIC (PATH FIRST)
        // =========================

        if (distance > MAX_HIDDEN_DISTANCE) {

            // Try normal navigation first
            if (mimic.canPathTo(targetB)) {

                mimic.moveToPlayer(targetB);

            } else {

                // Find valid ground position near target
                Vec3 groundPos = mimic.findGroundPositionNear(
                        targetB,
                        3.0,
                        5.0
                );

                if (groundPos != null && mimic.canPathTo(groundPos)) {

                    mimic.moveToPos(groundPos);

                } else {

                    // Last resort safe teleport (still grounded-ish)
                    BlockPos safe = targetB.blockPosition();

                    while (!level.getBlockState(safe).isSolid() &&
                            safe.getY() > level.getMinBuildHeight()) {
                        safe = safe.below();
                    }

                    mimic.teleportTo(
                            safe.getX() + 0.5,
                            safe.getY() + 1,
                            safe.getZ() + 0.5
                    );
                }
            }
        }

        else if (distance < STALK_TOO_CLOSE) {

            // Back off to a safe ground position
            Vec3 retreat = mimic.findGroundPositionNear(
                    targetB,
                    4.0,
                    6.0
            );

            if (retreat != null && mimic.canPathTo(retreat)) {
                mimic.moveToPos(retreat);
            } else {
                // fallback: reposition around target A instead of teleport chaos
                mimic.moveToPlayer(targetA);
            }
        }

        else {
            // In correct range → idle
            mimic.stopMoving();
        }

        // =========================
        // VOICE BEHAVIOUR
        // =========================

        if (random.nextFloat() < CHANCE_TO_SPEAK) {
            playVoiceToTarget(mimic, targetA, targetB);
        }

        // =========================
        // OCCASIONAL TARGET SWITCH
        // =========================

        if (random.nextFloat() < 0.001f) {

            ServerPlayer newTarget = findNearbyPlayer(mimic, targetA);

            if (newTarget != null &&
                    !newTarget.getUUID().equals(targetB.getUUID())) {

                mimic.setTargetBUUID(newTarget.getUUID().toString());
            }
        }
    }

    /**
     * Play a voice clip from Target A to Target B through the mimic
     */
    private void playVoiceToTarget(MimicEntity mimic, ServerPlayer targetA, ServerPlayer targetB) {
        if (mimic == null || targetA == null || targetB == null) return;

        VoiceHandler voiceHandler = VoiceHandler.getInstance();

        // NO clip retrieval anymore
        voiceHandler.replayVoice(mimic, targetA.getUUID());
    }

    /**
     * Find a nearby player to target (excluding the mimic's target A)
     */
    private ServerPlayer findNearbyPlayer(MimicEntity mimic, ServerPlayer exclude) {
        Level level = mimic.level();
        if (level == null || level.getServer() == null) return null;

        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
        
        // Filter to nearby players
        List<ServerPlayer> nearbyPlayers = new ArrayList<>();
        Vec3 mimicPos = mimic.position();

        for (ServerPlayer player : players) {
            if (player.isAlive() && !player.getUUID().equals(exclude != null ? exclude.getUUID() : null)) {
                double dist = mimicPos.distanceTo(player.position());
                if (dist < 32) { // Within 32 blocks
                    nearbyPlayers.add(player);
                }
            }
        }

        if (nearbyPlayers.isEmpty()) {
            return exclude; // Fall back to target A
        }

        return nearbyPlayers.get(random.nextInt(nearbyPlayers.size()));
    }

    /**
     * Try to spawn new mimics if needed
     */
    private void tryRespawnMimics(Level level) {
        if (level == null || level.getServer() == null) return;

        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
        
        if (players.isEmpty()) return;

        // Calculate how many mimics we should have
        int playerCount = players.size();
        int targetMimicCount = Math.min(playerCount, MAX_TOTAL_MIMICS);

        // Check current mimic count per target
        Map<UUID, Integer> mimicsPerTarget = new HashMap<>();
        for (MimicEntity mimic : activeMimics.values()) {
            String targetAUUID = mimic.getTargetAUUID();
            if (targetAUUID != null) {
                mimicsPerTarget.merge(UUID.fromString(targetAUUID), 1, Integer::sum);
            }
        }

        // Spawn mimics for players who don't have enough
        for (ServerPlayer player : players) {
            int currentMimics = mimicsPerTarget.getOrDefault(player.getUUID(), 0);
            
            if (currentMimics < MAX_MIMICS_PER_PLAYER && activeMimics.size() < MAX_TOTAL_MIMICS) {
                spawnMimicForPlayer(player, level);
            }
        }
    }

    /**
     * Spawn a mimic that targets a specific player
     */
    public MimicEntity spawnMimicForPlayer(ServerPlayer targetPlayer, Level level) {
        if (targetPlayer == null || level == null) return null;

        // Create the mimic
        MimicEntity mimic = ModEntities.get().mimic().create(level);
        if (mimic == null) return null;

        level.addFreshEntity(mimic);

        // Set Target A (the player to copy)
        mimic.setTargetAUUID(targetPlayer.getUUID().toString());
        
        // Copy the player's gamertag
        mimic.setCustomName(Component.literal(targetPlayer.getGameProfile().getName()));

        // Random spawn position near the target
        Vec3 targetPos = targetPlayer.position();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 8 + random.nextDouble() * 8; // 8-16 blocks away
        
        double spawnX = targetPos.x + Math.cos(angle) * distance;
        double spawnY = targetPos.y;
        double spawnZ = targetPos.z + Math.sin(angle) * distance;

        mimic.setPos(spawnX, spawnY, spawnZ);

        // Spawn the entity

        // Track it
        activeMimics.put(mimic.getUUID(), mimic);
        spawnTimes.put(mimic.getUUID(), System.currentTimeMillis());

        Constants.LOG.info("Spawned mimic for player " + targetPlayer.getGameProfile().getName());

        return mimic;
    }

    /**
     * Remove a mimic by UUID
     */
    public void removeMimic(UUID mimicId) {
        MimicEntity mimic = activeMimics.remove(mimicId);
        spawnTimes.remove(mimicId);

        if (mimic != null && mimic.isAlive()) {
            mimic.discard();
            Constants.LOG.info("Removed mimic " + mimicId);
        }
    }

    /**
     * Remove all mimics targeting a specific player
     */
    public void removeMimicsForPlayer(UUID playerId) {
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, MimicEntity> entry : activeMimics.entrySet()) {
            String targetAUUID = entry.getValue().getTargetAUUID();
            if (targetAUUID != null && targetAUUID.equals(playerId.toString())) {
                toRemove.add(entry.getKey());
            }
        }

        for (UUID id : toRemove) {
            removeMimic(id);
        }
    }

    /**
     * Get all active mimics
     */
    public Collection<MimicEntity> getActiveMimics() {
        return activeMimics.values();
    }

    /**
     * Get count of active mimics
     */
    public int getActiveMimicCount() {
        return activeMimics.size();
    }

    /**
     * Clear all mimics (for world unload, etc.)
     */
    public void clearAll() {
        for (MimicEntity mimic : activeMimics.values()) {
            if (mimic.isAlive()) {
                mimic.discard();
            }
        }
        activeMimics.clear();
        spawnTimes.clear();
    }
}