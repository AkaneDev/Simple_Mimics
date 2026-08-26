package au.akanedev.simplemimics.manager;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.api.events.MimicEvents;
import au.akanedev.simplemimics.api.events.callback.MimicMovementCallback;
import au.akanedev.simplemimics.entity.MimicEntity;
import au.akanedev.simplemimics.registry.ConfigRegistry;
import au.akanedev.simplemimics.registry.ModEntities;
import au.akanedev.simplemimics.util.MimicLocationUtil;
import au.akanedev.simplemimics.util.PlayerData;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MimicManager {

    private static MimicManager INSTANCE;
    private static ChatManager chatManager;

    private final Map<UUID, MimicEntity> activeMimics = new ConcurrentHashMap<>();
    private final Map<UUID, Long> spawnTimes = new ConcurrentHashMap<>();

    private static int MAX_MIMICS_PER_PLAYER = (int) ConfigRegistry.get("MAX_MIMICS_PER_PLAYER").get();
    private static int MAX_TOTAL_MIMICS = (int) ConfigRegistry.get("MAX_TOTAL_MIMICS").get();
    private static final long MIMIC_LIFETIME_MS = 8 * 60 * 1000;
    private static final int MIMIC_TICK_MOVEMENT_CHECK_MAX = 10;
    private static final long RESPAWN_INTERVAL_MS = 2 * 60 * 1000;

    private static float CHANCE_TO_SPEAK =
            (float) ConfigRegistry.get("CHANCE_TO_SPEAK").get();
    private static float CHANCE_TO_TEXT = (float) ConfigRegistry.get("CHANCE_TO_TEXT").get();


    private long lastRespawnCheck = 0;

    private static final double STALK_DISTANCE = 8.0;
    private static final double STALK_TOO_CLOSE = 3.0;
    private static final double STALK_TOO_FAR = 15.0;

    private static final double MIN_HIDDEN_DISTANCE = 4.0;
    private static final double MAX_HIDDEN_DISTANCE = 5.0;

    private final Random random = new Random();

    private MimicManager() {}

    public static MimicManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MimicManager();
        }
        if (chatManager == null) {
            chatManager = ChatManager.getInstance();
        }
        return INSTANCE;
    }

    public void onServerTick(MinecraftServer server) {
        if (server == null || server.getPlayerList() == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        CHANCE_TO_SPEAK =
                (float) ConfigRegistry.get("CHANCE_TO_SPEAK").get();
        MAX_MIMICS_PER_PLAYER = (int) ConfigRegistry.get("MAX_MIMICS_PER_PLAYER").get();
        MAX_TOTAL_MIMICS = (int) ConfigRegistry.get("MAX_TOTAL_MIMICS").get();
        CHANCE_TO_TEXT = (float) ConfigRegistry.get("CHANCE_TO_TEXT").get();

        if (currentTime - lastRespawnCheck > RESPAWN_INTERVAL_MS) {
            lastRespawnCheck = currentTime;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tryRespawnMimics(player.level());
                break;
            }
        }

        List<UUID> mimicsToRemove = new ArrayList<>();

        for (Map.Entry<UUID, MimicEntity> entry : activeMimics.entrySet()) {
            UUID mimicId = entry.getKey();
            MimicEntity mimic = entry.getValue();

            if (mimic == null || !mimic.isAlive()) {
                mimicsToRemove.add(mimicId);
                continue;
            }

            Long spawnTime = spawnTimes.get(mimicId);

            if (spawnTime != null &&
                    currentTime - spawnTime > MIMIC_LIFETIME_MS) {
                mimicsToRemove.add(mimicId);
                continue;
            }

            ServerPlayer targetA = mimic.getTargetA();

            if (targetA == null) {
                continue;
            }

            if (targetA.isSpectator()) {
                mimicsToRemove.add(mimicId);
                continue;
            }

            String name = targetA.getGameProfile().getName();

            if (!mimic.getName().getString().equals(name)) {
                mimic.setCustomName(Component.literal(name));
            }

            updateMimicBehavior(mimic, targetA);
        }

        for (UUID id : mimicsToRemove) {
            removeMimic(id);
        }
    }

    private void updateMimicBehavior(
            MimicEntity mimic,
            ServerPlayer targetA
    ) {
        if (mimic == null || targetA == null) {
            return;
        }

        if (!mimic.isAlive()) {
            UUID mimicUUID = mimic.getUUID();
            activeMimics.remove(mimicUUID);
            spawnTimes.remove(mimicUUID);
            return;
        }

        Level level = mimic.level();

        if (level == null) {
            return;
        }

        ServerPlayer targetB = mimic.getTargetB();

        if (targetB == null || !targetB.isAlive()) {
            if (random.nextFloat() < 0.7f) {
                targetB = findNearbyPlayer(mimic, targetA);
            } else {
                targetB = targetA;
            }

            if (targetB != null) {
                mimic.setTargetBUUID(
                        targetB.getUUID().toString()
                );
            }

            return;
        }

        MimicEvents.ACTION.invoke(
                event -> event.onAction(targetA, mimic)
        );

        double distance =
                mimic.position().distanceTo(targetB.position());

        mimicMovement(
                mimic,
                targetA,
                targetB,
                distance
        );
        chatrolls(targetA, targetB);

        if (random.nextFloat() < CHANCE_TO_SPEAK) {
            playVoiceToTarget(
                    mimic,
                    targetA,
                    targetB
            );
        }

        if (random.nextFloat() < 0.001f) {
            ServerPlayer newTarget =
                    findNearbyPlayer(mimic, targetA);

            if (newTarget != null &&
                    !newTarget.getUUID().equals(targetB.getUUID())) {

                AtomicBoolean allowed =
                        new AtomicBoolean(true);

                ServerPlayer finalTargetB = targetB;

                MimicEvents.TARGET_CHANGED.invoke(event -> {
                    if (!event.onTargetChanged(
                            mimic,
                            finalTargetB,
                            newTarget
                    )) {
                        allowed.set(false);
                    }
                });

                if (allowed.get()) {
                    mimic.setTargetBUUID(
                            newTarget.getUUID().toString()
                    );
                }
            }
        }


        if ((boolean) ConfigRegistry
                .get("ENABLE_ADDON_JUMPSCARES")
                .get()) {

            ServerPlayer finalTargetB = targetB;

            MimicEvents.JUMPSCARE_CALLBACK.invoke(
                    event -> event.onJumpscare(
                            mimic,
                            finalTargetB
                    )
            );
        }
    }

    private void chatrolls(
            ServerPlayer targetA,
            ServerPlayer targetB) {
        if (random.nextFloat() < CHANCE_TO_TEXT) {
            PlayerData pData = PlayerDataUtils.getPlayerData(targetA);
            if (pData != null) {
                Component message = chatManager.getRandomMessage(pData);
                if (message != null) {
                    PlayerDataUtils.sendMessageToOnePlayer(message, targetB);
                }
            }
        }
    }

    private void mimicMovement(
            MimicEntity mimic,
            ServerPlayer targetA,
            ServerPlayer targetB,
            double distance
    ) {
        if (mimic.getCurrentmovementtickcounter() < MIMIC_TICK_MOVEMENT_CHECK_MAX) {
            mimic.setCurrentmovementtickcounter(mimic.getCurrentmovementtickcounter() + 1);
            return;
        }
        mimic.setCurrentmovementtickcounter(0);
        Level level = mimic.getCommandSenderWorld();

        BlockPos mimicPos = mimic.blockPosition();
        BlockPos targetPos = targetB.blockPosition();

        if (distance > MAX_HIDDEN_DISTANCE) {

            Optional<BlockPos> approachPosition =
                    MimicLocationUtil.findHiddenLocation(
                            level,
                            targetPos,
                            targetB,
                            15,
                            0.50
                    );

            if (approachPosition.isPresent()) {
                BlockPos groundPos =
                        approachPosition.get();

                if (mimic.canPathTo(
                        Vec3.atBottomCenterOf(groundPos)
                )) {

                    MimicEvents.MOVEMENT.invoke(callback ->
                            callback.onMovement(
                                    MimicMovementCallback.MovementType.ADVANCE,
                                    targetB,
                                    mimic,
                                    mimic.blockPosition(),
                                    groundPos
                            )
                    );

                    mimic.moveToPos(
                            Vec3.atBottomCenterOf(groundPos)
                    );

                } else {

                    Vec3 targetPosition =
                            targetB.position();

                    if (mimic.canPathTo(targetPosition)) {

                        MimicEvents.MOVEMENT.invoke(callback ->
                                callback.onMovement(
                                        MimicMovementCallback.MovementType.ADVANCE,
                                        targetB,
                                        mimic,
                                        mimic.blockPosition(),
                                        targetPos
                                )
                        );

                        mimic.moveToPos(targetPosition);

                    } else {
                        mimic.moveToPlayer(targetB);
                    }
                }
            }

            return;
        }

        if (distance < STALK_TOO_CLOSE) {

            Optional<BlockPos> retreatPosition =
                    MimicLocationUtil.findHiddenLocation(
                            level,
                            targetPos,
                            targetB,
                            10,
                            0.60
                    );

            if (retreatPosition.isPresent()) {

                BlockPos hidePos =
                        retreatPosition.get();

                if (mimic.canPathTo(
                        Vec3.atBottomCenterOf(hidePos)
                )) {

                    MimicEvents.MOVEMENT.invoke(callback ->
                            callback.onMovement(
                                    MimicMovementCallback.MovementType.RETREAT,
                                    targetB,
                                    mimic,
                                    mimic.blockPosition(),
                                    hidePos
                            )
                    );

                    mimic.moveToPos(
                            Vec3.atBottomCenterOf(hidePos)
                    );

                } else {

                    Vec3 retreat =
                            mimic.findGroundPositionNear(
                                    targetB,
                                    4.0,
                                    8.0
                            );

                    if (retreat != null &&
                            mimic.canPathTo(retreat)) {

                        MimicEvents.MOVEMENT.invoke(callback ->
                                callback.onMovement(
                                        MimicMovementCallback.MovementType.RETREAT,
                                        targetB,
                                        mimic,
                                        mimic.blockPosition(),
                                        BlockPos.containing(retreat)
                                )
                        );

                        mimic.moveToPos(retreat);

                    } else {

                        mimic.moveToPlayer(targetA);

                        MimicEvents.MOVEMENT.invoke(callback ->
                                callback.onMovement(
                                        MimicMovementCallback.MovementType.RETREAT,
                                        targetA,
                                        mimic,
                                        mimic.blockPosition(),
                                        targetA.blockPosition()
                                )
                        );
                    }
                }
            }

            return;
        }

        double obscured =
                MimicLocationUtil.getObscuredRatio(
                        level,
                        targetB,
                        mimic.blockPosition()
                );

        if (obscured >= 0.60) {

            mimic.stopMoving();

            MimicEvents.MOVEMENT.invoke(callback ->
                    callback.onMovement(
                            MimicMovementCallback.MovementType.STOP,
                            targetB,
                            mimic,
                            mimic.blockPosition(),
                            BlockPos.ZERO
                    )
            );

            return;
        }

        Optional<BlockPos> hidePosition =
                MimicLocationUtil.findHiddenLocation(
                        level,
                        targetPos,
                        targetB,
                        12,
                        0.60
                );

        if (hidePosition.isPresent()) {

            BlockPos hidePos =
                    hidePosition.get();

            if (!hidePos.equals(mimic.blockPosition())) {

                Vec3 destination =
                        Vec3.atBottomCenterOf(hidePos);

                if (mimic.canPathTo(destination)) {

                    MimicEvents.MOVEMENT.invoke(callback ->
                            callback.onMovement(
                                    MimicMovementCallback.MovementType.ADVANCE,
                                    targetB,
                                    mimic,
                                    mimic.blockPosition(),
                                    hidePos
                            )
                    );

                    mimic.moveToPos(destination);

                } else {

                    Optional<BlockPos> alternate =
                            MimicLocationUtil.findHiddenLocation(
                                    level,
                                    mimic.blockPosition(),
                                    targetB,
                                    8,
                                    0.50
                            );

                    if (alternate.isPresent()) {

                        BlockPos alternatePos =
                                alternate.get();

                        Vec3 alternateDestination =
                                Vec3.atBottomCenterOf(
                                        alternatePos
                                );

                        if (mimic.canPathTo(
                                alternateDestination
                        )) {

                            MimicEvents.MOVEMENT.invoke(callback ->
                                    callback.onMovement(
                                            MimicMovementCallback.MovementType.ADVANCE,
                                            targetB,
                                            mimic,
                                            mimic.blockPosition(),
                                            alternatePos
                                    )
                            );

                            mimic.moveToPos(
                                    alternateDestination
                            );
                        }
                    }
                }

            } else {

                mimic.stopMoving();

                MimicEvents.MOVEMENT.invoke(callback ->
                        callback.onMovement(
                                MimicMovementCallback.MovementType.STOP,
                                targetB,
                                mimic,
                                mimic.blockPosition(),
                                BlockPos.ZERO
                        )
                );
            }

            return;
        }

        Vec3 fallback =
                mimic.findGroundPositionNear(
                        targetB,
                        Math.max(
                                STALK_TOO_CLOSE + 2.0,
                                5.0
                        ),
                        10.0
                );

        if (fallback != null &&
                mimic.canPathTo(fallback)) {

            MimicEvents.MOVEMENT.invoke(callback ->
                    callback.onMovement(
                            MimicMovementCallback.MovementType.ADVANCE,
                            targetB,
                            mimic,
                            mimic.blockPosition(),
                            BlockPos.containing(fallback)
                    )
            );

            mimic.moveToPos(fallback);

        } else {

            mimic.stopMoving();

            MimicEvents.MOVEMENT.invoke(callback ->
                    callback.onMovement(
                            MimicMovementCallback.MovementType.STOP,
                            targetB,
                            mimic,
                            mimic.blockPosition(),
                            BlockPos.ZERO
                    )
            );
        }
    }

    private void playVoiceToTarget(
            MimicEntity mimic,
            ServerPlayer targetA,
            ServerPlayer targetB
    ) {
        if (mimic == null ||
                targetA == null ||
                targetB == null) {
            return;
        }

        VoiceHandler voiceHandler =
                VoiceHandler.getInstance();

        AtomicBoolean allowed =
                new AtomicBoolean(true);

        MimicEvents.VOICE.invoke(event -> {
            if (!event.onVoice(
                    mimic,
                    targetA,
                    targetB
            )) {
                allowed.set(false);
            }
        });

        if (allowed.get()) {
            voiceHandler.replayVoice(
                    mimic,
                    targetA.getUUID()
            );
        }
    }

    private ServerPlayer findNearbyPlayer(
            MimicEntity mimic,
            ServerPlayer exclude
    ) {
        Level level = mimic.level();

        if (level == null ||
                level.getServer() == null) {
            return null;
        }

        List<ServerPlayer> players =
                level.getServer()
                        .getPlayerList()
                        .getPlayers();

        List<ServerPlayer> nearbyPlayers =
                new ArrayList<>();

        Vec3 mimicPos = mimic.position();

        UUID excludeUUID =
                exclude != null
                        ? exclude.getUUID()
                        : null;

        for (ServerPlayer player : players) {

            if (!player.isAlive()) {
                continue;
            }

            if (excludeUUID != null &&
                    player.getUUID().equals(excludeUUID)) {
                continue;
            }

            if (mimicPos.distanceTo(
                    player.position()
            ) < 32) {
                nearbyPlayers.add(player);
            }
        }

        if (nearbyPlayers.isEmpty()) {
            return exclude;
        }

        return nearbyPlayers.get(
                random.nextInt(
                        nearbyPlayers.size()
                )
        );
    }

    private void tryRespawnMimics(Level level) {
        if (level == null ||
                level.getServer() == null) {
            return;
        }

        List<ServerPlayer> players =
                level.getServer()
                        .getPlayerList()
                        .getPlayers();

        if (players.isEmpty()) {
            return;
        }

        int playerCount = players.size();

        int targetMimicCount =
                Math.min(
                        playerCount,
                        MAX_TOTAL_MIMICS
                );

        if (activeMimics.size() >= targetMimicCount) {
            return;
        }

        Map<UUID, Integer> mimicsPerTarget =
                new HashMap<>();

        for (MimicEntity mimic : activeMimics.values()) {

            String targetAUUID =
                    mimic.getTargetAUUID();

            if (targetAUUID == null ||
                    targetAUUID.isEmpty()) {
                continue;
            }

            try {
                UUID uuid =
                        UUID.fromString(targetAUUID);

                mimicsPerTarget.merge(
                        uuid,
                        1,
                        Integer::sum
                );

            } catch (IllegalArgumentException ignored) {
            }
        }

        for (ServerPlayer player : players) {

            int currentMimics =
                    mimicsPerTarget.getOrDefault(
                            player.getUUID(),
                            0
                    );

            if (currentMimics <
                    MAX_MIMICS_PER_PLAYER &&
                    activeMimics.size() <
                            MAX_TOTAL_MIMICS) {

                MimicEntity mimic =
                        spawnMimicForPlayer(
                                player,
                                player.level()
                        );

                if (mimic != null) {
                    mimicsPerTarget.merge(
                            player.getUUID(),
                            1,
                            Integer::sum
                    );
                }
            }

            if (activeMimics.size() >=
                    targetMimicCount) {
                break;
            }
        }
    }

    public MimicEntity spawnMimicForPlayer(
            ServerPlayer targetPlayer,
            Level level
    ) {
        if (targetPlayer == null ||
                level == null) {
            return null;
        }

        MimicEntity mimic =
                ModEntities.get()
                        .mimic()
                        .create(level);

        if (mimic == null) {
            return null;
        }

        level.addFreshEntity(mimic);

        mimic.setTargetAUUID(
                targetPlayer.getUUID().toString()
        );

        mimic.setCustomName(
                Component.literal(
                        targetPlayer
                                .getGameProfile()
                                .getName()
                )
        );

        Vec3 targetPos =
                targetPlayer.position();

        double angle =
                random.nextDouble() *
                        2 *
                        Math.PI;

        double distance =
                8 +
                        random.nextDouble() * 8;

        double spawnX =
                targetPos.x +
                        Math.cos(angle) *
                                distance;

        double spawnY =
                targetPos.y;

        double spawnZ =
                targetPos.z +
                        Math.sin(angle) *
                                distance;

        mimic.setPos(
                spawnX,
                spawnY,
                spawnZ
        );

        activeMimics.put(
                mimic.getUUID(),
                mimic
        );

        spawnTimes.put(
                mimic.getUUID(),
                System.currentTimeMillis()
        );

        Constants.LOG.info(
                "Spawned mimic for player " +
                        targetPlayer
                                .getGameProfile()
                                .getName()
        );

        MimicEvents.CREATED.invoke(
                event -> event.onCreated(
                        mimic,
                        targetPlayer
                )
        );

        return mimic;
    }

    public void removeMimic(UUID mimicId) {
        MimicEntity mimic =
                activeMimics.remove(mimicId);

        spawnTimes.remove(mimicId);

        if (mimic != null &&
                mimic.isAlive()) {

            MimicEvents.REMOVED.invoke(
                    event -> event.onRemoved(mimic)
            );

            mimic.discard();

            Constants.LOG.info(
                    "Removed mimic " +
                            mimicId
            );
        }
    }

    public void removeMimicsForPlayer(UUID playerId) {
        List<UUID> toRemove =
                new ArrayList<>();

        for (Map.Entry<UUID, MimicEntity> entry :
                activeMimics.entrySet()) {

            String targetAUUID =
                    entry.getValue()
                            .getTargetAUUID();

            if (targetAUUID != null &&
                    targetAUUID.equals(
                            playerId.toString()
                    )) {

                toRemove.add(
                        entry.getKey()
                );
            }
        }

        for (UUID id : toRemove) {
            removeMimic(id);
        }
    }

    public Collection<MimicEntity> getActiveMimics() {
        return activeMimics.values();
    }

    public int getActiveMimicCount() {
        return activeMimics.size();
    }

    public void clearAll() {
        for (MimicEntity mimic :
                activeMimics.values()) {

            if (mimic.isAlive()) {
                mimic.discard();
            }
        }

        activeMimics.clear();
        spawnTimes.clear();
    }
}