package au.akanedev.simplemimics.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Optional;

public final class MimicLocationUtil {

    private MimicLocationUtil() {
    }

    private static final double MIMIC_WIDTH = 0.6;
    private static final double MIMIC_HEIGHT = 1.95;

    private static final double DEFAULT_MIN_OBSCURED = 0.5;

    private static final double FULL_COVER = 1.0;
    private static final double LEAF_COVER = 0.8;

    public static Optional<BlockPos> findHiddenLocation(
            Level level,
            BlockPos origin,
            ServerPlayer player,
            int radius
    ) {
        return findHiddenLocation(
                level,
                origin,
                player,
                radius,
                DEFAULT_MIN_OBSCURED
        );
    }

    public static Optional<BlockPos> findHiddenLocation(
            Level level,
            BlockPos origin,
            ServerPlayer player,
            int radius,
            double minimumObscured
    ) {
        if (level == null ||
                origin == null ||
                player == null) {
            return Optional.empty();
        }

        if (radius < 0) {
            return Optional.empty();
        }

        minimumObscured = Math.max(
                0.0,
                Math.min(1.0, minimumObscured)
        );

        Vec3 playerFeet = player.position();
        Vec3 playerEyes = player.getEyePosition();

        BlockPos bestPosition = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        int radiusSquared = radius * radius;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if (x * x + y * y + z * z > radiusSquared) {
                        continue;
                    }

                    BlockPos candidate =
                            origin.offset(x, y, z);

                    if (!isValidMimicLocation(
                            level,
                            candidate
                    )) {
                        continue;
                    }

                    double cheapCover =
                            getCheapCoverScore(
                                    level,
                                    playerFeet,
                                    playerEyes,
                                    candidate
                            );

                    if (cheapCover < minimumObscured) {
                        continue;
                    }

                    double obscured =
                            getObscuredRatio(
                                    level,
                                    playerFeet,
                                    playerEyes,
                                    candidate
                            );

                    if (obscured < minimumObscured) {
                        continue;
                    }

                    double distance = Math.sqrt(
                            candidate.distSqr(origin)
                    );

                    double score =
                            obscured * 100.0
                                    + cheapCover * 10.0
                                    - distance * 0.25;

                    if (score > bestScore) {
                        bestScore = score;
                        bestPosition = candidate;
                    }
                }
            }
        }

        return Optional.ofNullable(bestPosition);
    }

    public static boolean isValidMimicLocation(
            Level level,
            BlockPos pos
    ) {
        if (level == null || pos == null) {
            return false;
        }

        AABB box = getMimicAABB(pos);

        if (!level.noCollision(box)) {
            return false;
        }

        BlockPos floorPos = pos.below();

        if (level.getBlockState(floorPos)
                .getCollisionShape(level, floorPos)
                .isEmpty()) {
            return false;
        }

        Block block =
                level.getBlockState(pos).getBlock();

        if (block == Blocks.WATER ||
                block == Blocks.LAVA) {
            return false;
        }

        return true;
    }

    private static double getCheapCoverScore(
            Level level,
            Vec3 playerFeet,
            Vec3 playerEyes,
            BlockPos candidate
    ) {
        Vec3 target =
                Vec3.atCenterOf(candidate);

        double feetScore =
                getLineCoverScore(
                        level,
                        playerFeet,
                        target
                );

        double eyesScore =
                getLineCoverScore(
                        level,
                        playerEyes,
                        target
                );

        return Math.max(
                feetScore,
                eyesScore
        );
    }

    private static double getLineCoverScore(
            Level level,
            Vec3 start,
            Vec3 end
    ) {
        Vec3 difference =
                end.subtract(start);

        double distance =
                difference.length();

        if (distance <= 0.001) {
            return 0.0;
        }

        int steps = Math.max(
                1,
                (int) Math.ceil(distance)
        );

        double bestScore = 0.0;

        for (int i = 1; i < steps; i++) {

            double progress =
                    (double) i / steps;

            Vec3 point =
                    start.lerp(
                            end,
                            progress
                    );

            BlockPos pos =
                    BlockPos.containing(point);

            double cover =
                    getBlockCoverScore(
                            level,
                            pos
                    );

            if (cover > bestScore) {
                bestScore = cover;
            }

            if (bestScore >= FULL_COVER) {
                return bestScore;
            }
        }

        return bestScore;
    }

    private static double getBlockCoverScore(
            Level level,
            BlockPos pos
    ) {
        var state =
                level.getBlockState(pos);

        Block block =
                state.getBlock();

        if (block == Blocks.AIR ||
                block == Blocks.CAVE_AIR ||
                block == Blocks.VOID_AIR) {
            return 0.0;
        }

        if (block == Blocks.WATER ||
                block == Blocks.LAVA) {
            return 0.0;
        }

        if (block instanceof LeavesBlock) {
            return LEAF_COVER;
        }

        if (isTransparentBlock(block)) {
            return 0.0;
        }

        if (state.getCollisionShape(
                level,
                pos
        ).isEmpty()) {
            return 0.0;
        }

        return FULL_COVER;
    }

    private static boolean isTransparentBlock(
            Block block
    ) {
        return block == Blocks.GLASS
                || block == Blocks.GLASS_PANE
                || block == Blocks.TINTED_GLASS
                || block == Blocks.ICE
                || block == Blocks.PACKED_ICE
                || block == Blocks.BLUE_ICE
                || block == Blocks.SLIME_BLOCK
                || block == Blocks.HONEY_BLOCK;
    }

    public static double getObscuredRatio(
            Level level,
            ServerPlayer player,
            BlockPos position
    ) {
        if (level == null ||
                player == null ||
                position == null) {
            return 0.0;
        }

        return getObscuredRatio(
                level,
                player.position(),
                player.getEyePosition(),
                position
        );
    }

    private static double getObscuredRatio(
            Level level,
            Vec3 playerFeet,
            Vec3 playerEyes,
            BlockPos position
    ) {
        Vec3 target =
                Vec3.atCenterOf(position);

        boolean feetBlocked =
                isPointObscured(
                        level,
                        playerFeet,
                        target
                );

        boolean eyesBlocked =
                isPointObscured(
                        level,
                        playerEyes,
                        target
                );

        if (feetBlocked && eyesBlocked) {
            return 1.0;
        }

        if (feetBlocked || eyesBlocked) {
            return 0.5;
        }

        return 0.0;
    }

    public static boolean isPointObscured(
            Level level,
            Vec3 viewpoint,
            Vec3 target
    ) {
        BlockHitResult result =
                level.clip(
                        new ClipContext(
                                viewpoint,
                                target,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                CollisionContext.empty()
                        )
                );

        if (result.getType() !=
                BlockHitResult.Type.BLOCK) {
            return false;
        }

        double hitDistance =
                result.getLocation()
                        .distanceToSqr(viewpoint);

        double targetDistance =
                target.distanceToSqr(viewpoint);

        return hitDistance <
                targetDistance - 0.0001;
    }

    public static AABB getMimicAABB(
            BlockPos position
    ) {
        double centreX =
                position.getX() + 0.5;

        double centreZ =
                position.getZ() + 0.5;

        double halfWidth =
                MIMIC_WIDTH / 2.0;

        return new AABB(
                centreX - halfWidth,
                position.getY(),
                centreZ - halfWidth,

                centreX + halfWidth,
                position.getY() + MIMIC_HEIGHT,
                centreZ + halfWidth
        );
    }

    public static boolean isMostlyHidden(
            Level level,
            BlockPos position,
            ServerPlayer player
    ) {
        return getObscuredRatio(
                level,
                player,
                position
        ) >= DEFAULT_MIN_OBSCURED;
    }

    public static boolean isMostlyHidden(
            Level level,
            BlockPos position,
            ServerPlayer player,
            double minimumObscured
    ) {
        minimumObscured = Math.max(
                0.0,
                Math.min(1.0, minimumObscured)
        );

        return getObscuredRatio(
                level,
                player,
                position
        ) >= minimumObscured;
    }

    public static Vec3 getMimicPosition(
            BlockPos position
    ) {
        return new Vec3(
                position.getX() + 0.5,
                position.getY(),
                position.getZ() + 0.5
        );
    }

    public static double getDefaultMinimumObscured() {
        return DEFAULT_MIN_OBSCURED;
    }
}