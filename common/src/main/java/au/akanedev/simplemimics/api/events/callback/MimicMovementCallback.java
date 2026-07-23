package au.akanedev.simplemimics.api.events.callback;

import au.akanedev.simplemimics.entity.MimicEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MimicMovementCallback {

    void onMovement(
            MovementType type,
            ServerPlayer target,
            MimicEntity mimic,
            BlockPos currentPos,
            BlockPos targetPos
    );


    static MimicMovementCallback onRetreat(
            MovementHandler handler
    ) {
        return (type, target, mimic, currentPos, targetPos) -> {
            if (type == MovementType.RETREAT) {
                handler.handle(
                        target,
                        mimic,
                        currentPos,
                        targetPos
                );
            }
        };
    }


    static MimicMovementCallback onAdvance(
            MovementHandler handler
    ) {
        return (type, target, mimic, currentPos, targetPos) -> {
            if (type == MovementType.ADVANCE) {
                handler.handle(
                        target,
                        mimic,
                        currentPos,
                        targetPos
                );
            }
        };
    }


    static MimicMovementCallback onTeleport(
            MovementHandler handler
    ) {
        return (type, target, mimic, currentPos, targetPos) -> {
            if (type == MovementType.TELEPORT) {
                handler.handle(
                        target,
                        mimic,
                        currentPos,
                        targetPos
                );
            }
        };
    }
    static MimicMovementCallback onStop(
            MovementHandler handler
    ) {
        return (type, target, mimic, pos, unused) -> {
            if (type == MovementType.STOP) {
                handler.handle(
                        target,
                        mimic,
                        pos,
                        pos
                );
            }
        };
    }


    @FunctionalInterface
    interface MovementHandler {

        void handle(
                ServerPlayer target,
                MimicEntity mimic,
                BlockPos currentPos,
                BlockPos targetPos
        );

    }


    enum MovementType {
        RETREAT,
        ADVANCE,
        TELEPORT,
        STOP
    }
}