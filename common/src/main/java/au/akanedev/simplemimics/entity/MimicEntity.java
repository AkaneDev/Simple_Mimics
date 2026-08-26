package au.akanedev.simplemimics.entity;

import au.akanedev.simplemimics.voice.VoiceHandler;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MimicEntity extends PathfinderMob {

    // =========================
    // TARGET DATA
    // =========================

    private static final EntityDataAccessor<String> DATA_TARGET_A =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.STRING
            );

    private static final EntityDataAccessor<String> DATA_TARGET_B =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.STRING
            );

    // =========================
    // EQUIPMENT SYNC DATA
    // =========================

    private static final EntityDataAccessor<ItemStack> DATA_MAIN_HAND =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.ITEM_STACK
            );

    private static final EntityDataAccessor<ItemStack> DATA_OFF_HAND =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.ITEM_STACK
            );

    private static final EntityDataAccessor<ItemStack> DATA_HELMET =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.ITEM_STACK
            );

    private static final EntityDataAccessor<ItemStack> DATA_CHESTPLATE =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.ITEM_STACK
            );

    private static final EntityDataAccessor<ItemStack> DATA_LEGGINGS =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.ITEM_STACK
            );

    private static final EntityDataAccessor<ItemStack> DATA_BOOTS =
            SynchedEntityData.defineId(
                    MimicEntity.class,
                    EntityDataSerializers.ITEM_STACK
            );

    // =========================
    // TARGET STATE
    // =========================

    private String pendingA;
    private String pendingB;

    // =========================
    // VOICE
    // =========================

    private EntityAudioChannel voiceChannel;

    // =========================
    // STATE
    // =========================

    private boolean synced = false;
    private boolean mimicAddedToWorld = false;

    private int currentmovementtickcounter = 0;

    // =========================
    // INVENTORY
    // =========================

    private final NonNullList<ItemStack> mimicInventory =
            NonNullList.withSize(
                    36,
                    ItemStack.EMPTY
            );

    private int mimicSelectedSlot = 0;

    private HumanoidArm mimicMainArm =
            HumanoidArm.RIGHT;

    private int equipmentSyncTimer = 0;

    // =========================
    // CONSTRUCTOR
    // =========================

    public MimicEntity(
            net.minecraft.world.entity.EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(type, level);

        this.setNoGravity(false);
    }

    // =========================
    // TARGET RESOLVE
    // =========================

    public ServerPlayer getTargetA() {
        Player player = resolvePlayer(getTargetAUUID());

        return player instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : null;
    }

    public ServerPlayer getTargetB() {
        Player player = resolvePlayer(getTargetBUUID());

        return player instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : null;
    }

    private Player resolvePlayer(String uuidStr) {
        if (uuidStr == null || uuidStr.isEmpty()) {
            return null;
        }

        try {
            UUID uuid = UUID.fromString(uuidStr);

            if (this.level()
                    instanceof net.minecraft.server.level.ServerLevel serverLevel) {

                return serverLevel
                        .getServer()
                        .getPlayerList()
                        .getPlayer(uuid);
            }

        } catch (Exception ignored) {
        }

        return null;
    }

    // =========================
    // VOICE
    // =========================

    private EntityAudioChannel createMimicVoice(
            MimicEntity mimic
    ) {
        VoicechatServerApi api =
                VoiceHandler
                        .getInstance()
                        .getApi();

        EntityAudioChannel channel =
                api.createEntityAudioChannel(
                        UUID.randomUUID(),
                        api.fromEntity(mimic)
                );

        if (channel != null) {
            channel.setDistance(32);
            channel.setCategory("mimic");
        }

        return channel;
    }

    public EntityAudioChannel getVoiceChannel() {
        return this.voiceChannel;
    }

    // =========================
    // TARGET UUIDS
    // =========================

    public String getTargetAUUID() {
        return this.entityData.get(DATA_TARGET_A);
    }

    public String getTargetBUUID() {
        return this.entityData.get(DATA_TARGET_B);
    }

    // =========================
    // TARGET SETTERS
    // =========================

    public void setTargetAUUID(String uuid) {
        if (!this.isMimicAddedToWorld()) {
            pendingA = uuid;
            return;
        }

        this.entityData.set(
                DATA_TARGET_A,
                uuid
        );
    }

    public void setTargetBUUID(String uuid) {
        if (!this.isMimicAddedToWorld()) {
            pendingB = uuid;
            return;
        }

        this.entityData.set(
                DATA_TARGET_B,
                uuid
        );
    }

    public boolean isMimicAddedToWorld() {
        return this.mimicAddedToWorld;
    }

    // =========================
    // COPY PLAYER INVENTORY
    // =========================

    public void copyPlayerInventory(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        for (int i = 0; i < 36; i++) {
            mimicInventory.set(
                    i,
                    player.getInventory()
                            .getItem(i)
                            .copy()
            );
        }

        mimicSelectedSlot =
                player.getInventory().selected;

        mimicMainArm =
                player.getMainArm();
    }

    // =========================
    // COPY PLAYER EQUIPMENT
    // =========================

    public void copyPlayerEquipment(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        // Main hand
        this.entityData.set(
                DATA_MAIN_HAND,
                player.getMainHandItem().copy()
        );

        // Offhand
        this.entityData.set(
                DATA_OFF_HAND,
                player.getOffhandItem().copy()
        );

        /*
         * Player armour inventory:
         *
         * 0 = boots
         * 1 = leggings
         * 2 = chestplate
         * 3 = helmet
         */

        this.entityData.set(
                DATA_BOOTS,
                player.getInventory()
                        .getArmor(0)
                        .copy()
        );

        this.entityData.set(
                DATA_LEGGINGS,
                player.getInventory()
                        .getArmor(1)
                        .copy()
        );

        this.entityData.set(
                DATA_CHESTPLATE,
                player.getInventory()
                        .getArmor(2)
                        .copy()
        );

        this.entityData.set(
                DATA_HELMET,
                player.getInventory()
                        .getArmor(3)
                        .copy()
        );

        mimicSelectedSlot =
                player.getInventory().selected;

        mimicMainArm =
                player.getMainArm();
    }

    public void copyPlayerInventoryAndEquipment(
            ServerPlayer player
    ) {
        copyPlayerInventory(player);
        copyPlayerEquipment(player);
    }

    // =========================
    // EQUIPMENT API
    // =========================

    /**
     * This is the important part for vanilla
     * armour/elytra rendering.
     */
    @Override
    public ItemStack getItemBySlot(
            EquipmentSlot slot
    ) {
        return switch (slot) {
            case HEAD ->
                    this.entityData.get(DATA_HELMET);

            case CHEST ->
                    this.entityData.get(DATA_CHESTPLATE);

            case LEGS ->
                    this.entityData.get(DATA_LEGGINGS);

            case FEET ->
                    this.entityData.get(DATA_BOOTS);

            case MAINHAND ->
                    this.entityData.get(DATA_MAIN_HAND);

            case OFFHAND ->
                    this.entityData.get(DATA_OFF_HAND);
            default -> ItemStack.EMPTY;
        };
    }

    public ItemStack getRandomItem() {
        List<ItemStack> nonEmptyItems = new ArrayList<>();

        // Normal inventory
        for (ItemStack stack : mimicInventory) {
            if (!stack.isEmpty()) {
                nonEmptyItems.add(stack);
            }
        }

        // Armour
        ItemStack helmet = getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = getItemBySlot(EquipmentSlot.FEET);

        if (!helmet.isEmpty()) {
            nonEmptyItems.add(helmet);
        }

        if (!chestplate.isEmpty()) {
            nonEmptyItems.add(chestplate);
        }

        if (!leggings.isEmpty()) {
            nonEmptyItems.add(leggings);
        }

        if (!boots.isEmpty()) {
            nonEmptyItems.add(boots);
        }

        if (nonEmptyItems.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return nonEmptyItems.get(
                this.random.nextInt(nonEmptyItems.size())
        ).copy();
    }

    @Override
    protected void dropCustomDeathLoot(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source,
            boolean recentlyHit
    ) {
        this.spawnAtLocation(getRandomItem());
    }

    @Override
    public ItemStack getMainHandItem() {
        return getItemBySlot(
                EquipmentSlot.MAINHAND
        );
    }

    @Override
    public ItemStack getOffhandItem() {
        return getItemBySlot(
                EquipmentSlot.OFFHAND
        );
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of(
                getItemBySlot(
                        EquipmentSlot.FEET
                ),
                getItemBySlot(
                        EquipmentSlot.LEGS
                ),
                getItemBySlot(
                        EquipmentSlot.CHEST
                ),
                getItemBySlot(
                        EquipmentSlot.HEAD
                )
        );
    }

    @Override
    public HumanoidArm getMainArm() {
        return mimicMainArm;
    }

    // =========================
    // INVENTORY ACCESS
    // =========================

    public ItemStack getMimicItem(
            int slot
    ) {
        if (slot < 0 ||
                slot >= mimicInventory.size()) {
            return ItemStack.EMPTY;
        }

        return mimicInventory.get(slot);
    }

    public NonNullList<ItemStack> getMimicInventory() {
        return mimicInventory;
    }

    public int getMimicSelectedSlot() {
        return mimicSelectedSlot;
    }

    public HumanoidArm getMimicMainArm() {
        return mimicMainArm;
    }

    // =========================
    // TICK
    // =========================

    @Override
    public void tick() {
        super.tick();

        // =========================
        // WORLD INITIALISATION
        // =========================

        if (!mimicAddedToWorld &&
                !level().isClientSide) {

            mimicAddedToWorld = true;

            this.voiceChannel =
                    createMimicVoice(this);
        }

        // =========================
        // FLUSH PENDING TARGET DATA
        // =========================

        if (!synced &&
                this.isMimicAddedToWorld()) {

            if (pendingA != null) {
                this.entityData.set(
                        DATA_TARGET_A,
                        pendingA
                );

                pendingA = null;
            }

            if (pendingB != null) {
                this.entityData.set(
                        DATA_TARGET_B,
                        pendingB
                );

                pendingB = null;
            }

            synced = true;
        }

        // =========================
        // TARGET
        // =========================

        Player a = getTargetA();

        // =========================
        // INVENTORY + EQUIPMENT SYNC
        // =========================

        if (a instanceof ServerPlayer serverPlayer) {

            if (++equipmentSyncTimer >= 10) {

                equipmentSyncTimer = 0;

                copyPlayerInventoryAndEquipment(
                        serverPlayer
                );
            }
        }

        // =========================
        // LOOK SYSTEM
        // =========================

        Player b = getTargetB();

        Player lookTarget =
                (b != null)
                        ? b
                        : a;

        if (lookTarget != null) {

            this.getLookControl().setLookAt(
                    lookTarget.getX(),
                    lookTarget.getEyeY(),
                    lookTarget.getZ(),
                    30.0F,
                    30.0F
            );
        }
    }

    // =========================
    // SYNC DATA
    // =========================

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(
                DATA_TARGET_A,
                ""
        );

        builder.define(
                DATA_TARGET_B,
                ""
        );

        builder.define(
                DATA_MAIN_HAND,
                ItemStack.EMPTY
        );

        builder.define(
                DATA_OFF_HAND,
                ItemStack.EMPTY
        );

        builder.define(
                DATA_HELMET,
                ItemStack.EMPTY
        );

        builder.define(
                DATA_CHESTPLATE,
                ItemStack.EMPTY
        );

        builder.define(
                DATA_LEGGINGS,
                ItemStack.EMPTY
        );

        builder.define(
                DATA_BOOTS,
                ItemStack.EMPTY
        );
    }

    // =========================
    // SAVE / LOAD
    // =========================

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putString(
                "TargetA",
                getTargetAUUID()
        );

        tag.putString(
                "TargetB",
                getTargetBUUID()
        );
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        pendingA =
                tag.getString("TargetA");

        pendingB =
                tag.getString("TargetB");

        synced = false;
    }

    // =========================
    // ATTRIBUTES
    // =========================

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        64.0
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.28
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        64.0
                );
    }

    // =========================
    // MOVEMENT
    // =========================

    public void stopMoving() {
        this.getNavigation().stop();
    }

    public Vec3 findGroundPositionNear(
            Player player,
            double minDist,
            double maxDist
    ) {
        Level level = this.level();

        for (int attempt = 0;
             attempt < 15;
             attempt++) {

            double angle =
                    this.random.nextDouble()
                            * Math.PI * 2;

            double distance =
                    minDist +
                            this.random.nextDouble()
                                    * (maxDist - minDist);

            double x =
                    player.getX() +
                            Math.cos(angle)
                                    * distance;

            double z =
                    player.getZ() +
                            Math.sin(angle)
                                    * distance;

            int y =
                    player.getBlockY() + 8;

            BlockPos.MutableBlockPos pos =
                    new BlockPos.MutableBlockPos(
                            (int) x,
                            y,
                            (int) z
                    );

            while (
                    pos.getY()
                            > level.getMinBuildHeight()
            ) {

                BlockPos below =
                        pos.below();

                if (
                        level.getBlockState(below)
                                .isSolid()
                                &&
                                level.getBlockState(pos)
                                        .isAir()
                                &&
                                level.getBlockState(pos.above())
                                        .isAir()
                ) {

                    return new Vec3(
                            pos.getX() + 0.5,
                            pos.getY(),
                            pos.getZ() + 0.5
                    );
                }

                pos.move(0, -1, 0);
            }
        }

        return null;
    }

    public void moveTo(
            Vec3 position,
            double speed
    ) {
        this.getNavigation().moveTo(
                position.x,
                position.y,
                position.z,
                speed
        );
    }

    public void moveTo(
            Player player,
            double speed
    ) {
        this.getNavigation().moveTo(
                player,
                speed
        );
    }

    public void moveToPlayer(
            Player player
    ) {
        this.getNavigation().moveTo(
                player,
                1.1
        );
    }

    public void moveToPos(
            Vec3 pos
    ) {
        this.getNavigation().moveTo(
                pos.x,
                pos.y,
                pos.z,
                1.1
        );
    }

    public void moveAwayFrom(
            Player player,
            double distance
    ) {
        Vec3 myPos =
                this.position();

        Vec3 dir =
                myPos.subtract(
                        player.position()
                ).normalize();

        Vec3 target =
                myPos.add(
                        dir.scale(distance)
                );

        this.getNavigation().moveTo(
                target.x,
                target.y,
                target.z,
                1.0
        );
    }

    public boolean canPathTo(
            Vec3 pos
    ) {
        Path path =
                this.getNavigation().createPath(
                        BlockPos.containing(pos),
                        0
                );

        return path != null;
    }

    public boolean canPathTo(
            Player player
    ) {
        Path path =
                this.getNavigation().createPath(
                        player,
                        0
                );

        return path != null &&
                path.canReach();
    }

    // =========================
    // MOVEMENT COUNTER
    // =========================

    public int getCurrentmovementtickcounter() {
        return currentmovementtickcounter;
    }

    public void setCurrentmovementtickcounter(
            int currentmovementtickcounter
    ) {
        this.currentmovementtickcounter =
                currentmovementtickcounter;
    }
}