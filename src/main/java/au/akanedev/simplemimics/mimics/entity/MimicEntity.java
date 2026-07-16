package au.akanedev.simplemimics.mimics.entity;

import au.akanedev.simplemimics.mimics.voice.VoiceHandler;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MimicEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_TARGET_A =
            SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_TARGET_B =
            SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.STRING);

    private String pendingA;
    private String pendingB;
    private EntityAudioChannel voiceChannel;
    private boolean synced = false;

    public MimicEntity(net.minecraft.world.entity.EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);

        this.setNoGravity(false);
    }

    // =========================
    // TARGET RESOLVE
    // =========================

    public Player getTargetA() {
        return resolvePlayer(getTargetAUUID());
    }

    public Player getTargetB() {
        return resolvePlayer(getTargetBUUID());
    }

    private Player resolvePlayer(String uuidStr) {
        if (uuidStr == null || uuidStr.isEmpty()) return null;

        try {
            UUID uuid = UUID.fromString(uuidStr);

            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                return serverLevel.getServer().getPlayerList().getPlayer(uuid);
            }
        } catch (Exception ignored) {}

        return null;
    }

    private EntityAudioChannel createMimicVoice(MimicEntity mimic) {
        VoicechatServerApi api = VoiceHandler.getInstance().getApi();
        EntityAudioChannel channel = api.createEntityAudioChannel(
                UUID.randomUUID(),
                api.fromEntity(mimic)
        );

        if (channel != null) {
            channel.setDistance(32);
            channel.setCategory("mimic");
        }

        return channel;
    }

    public String getTargetAUUID() {
        return this.entityData.get(DATA_TARGET_A);
    }

    public String getTargetBUUID() {
        return this.entityData.get(DATA_TARGET_B);
    }

    public EntityAudioChannel getVoiceChannel() {return this.voiceChannel;}

    // =========================
    // SAFE SETTERS
    // =========================

    public void setTargetAUUID(String uuid) {
        if (!this.isAddedToWorld()) {
            pendingA = uuid;
            return;
        }
        this.entityData.set(DATA_TARGET_A, uuid);
    }


    public void setTargetBUUID(String uuid) {
        if (!this.isAddedToWorld()) {
            pendingB = uuid;
            return;
        }
        this.entityData.set(DATA_TARGET_B, uuid);
    }

    // =========================
    // TICK (FIXED PHYSICS)
    // =========================

    @Override
    public void tick() {
        super.tick();

        // flush pending sync once world-ready
        if (!synced && this.isAddedToWorld()) {

            if (pendingA != null) {
                this.entityData.set(DATA_TARGET_A, pendingA);
                pendingA = null;
            }

            if (pendingB != null) {
                this.entityData.set(DATA_TARGET_B, pendingB);
                pendingB = null;
            }

            synced = true;
        }

        // =========================
        // LOOK SYSTEM
        // =========================

        Player a = getTargetA();
        Player b = getTargetB();

        Player lookTarget = (b != null) ? b : a;

        if (lookTarget != null) {
            this.getLookControl().setLookAt(
                    lookTarget.getX(),
                    lookTarget.getEyeY(),
                    lookTarget.getZ(),
                    30.0f,
                    30.0f
            );
        }
    }

    // =========================
    // SYNCHED DATA
    // =========================

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        if (!level().isClientSide) {
            this.voiceChannel = createMimicVoice(this);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_335882_) {
        super.defineSynchedData(p_335882_);
        p_335882_.define(DATA_TARGET_A, "");
        p_335882_.define(DATA_TARGET_B, "");
    }

    // =========================
    // SAVE / LOAD
    // =========================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("TargetA", getTargetAUUID());
        tag.putString("TargetB", getTargetBUUID());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        pendingA = tag.getString("TargetA");
        pendingB = tag.getString("TargetB");
        synced = false;
    }

    // =========================
    // ATTRIBUTES
    // =========================

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    public void stopMoving() {
        this.getNavigation().stop();
    }

    public Vec3 findGroundPositionNear(Player player, double minDist, double maxDist) {
        Level level = this.level();

        for (int attempt = 0; attempt < 15; attempt++) {

            double angle = this.random.nextDouble() * Math.PI * 2;

            double distance = minDist + this.random.nextDouble() * (maxDist - minDist);

            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;

            int y = player.getBlockY() + 8; // start above player

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((int)x, y, (int)z);

            // Drop down to ground
            while (pos.getY() > level.getMinBuildHeight()) {

                BlockPos below = pos.below();

                if (level.getBlockState(below).isSolid() &&
                        level.getBlockState(pos).isAir() &&
                        level.getBlockState(pos.above()).isAir()) {

                    return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                }

                pos.move(0, -1, 0);
            }
        }

        return null;
    }

    public void moveTo(Vec3 position, double speed) {
        this.getNavigation().moveTo(
                position.x,
                position.y,
                position.z,
                speed
        );
    }

    public void moveTo(Player player, double speed) {
        this.getNavigation().moveTo(player, speed);
    }
    public void moveToPlayer(Player player) {
        this.getNavigation().moveTo(player, 1.1);
    }

    public void moveToPos(Vec3 pos) {
        this.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.1);
    }


    public void moveAwayFrom(Player player, double distance) {
        Vec3 myPos = this.position();

        Vec3 dir = myPos.subtract(player.position()).normalize();

        Vec3 target = myPos.add(dir.scale(distance));

        this.getNavigation().moveTo(
                target.x,
                target.y,
                target.z,
                1.0
        );
    }

    public boolean canPathTo(Vec3 pos) {
        Path path = this.getNavigation().createPath(
                BlockPos.containing(pos),
                0
        );

        return path != null;
    }

    public boolean canPathTo(Player player) {
        Path path = this.getNavigation().createPath(player, 0);

        return path != null && path.canReach();
    }
}