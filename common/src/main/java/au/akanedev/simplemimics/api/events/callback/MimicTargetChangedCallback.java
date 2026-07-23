package au.akanedev.simplemimics.api.events.callback;

import au.akanedev.simplemimics.entity.MimicEntity;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MimicTargetChangedCallback {

    /**
     * Return false to prevent target change
     */
    boolean onTargetChanged(
            MimicEntity mimic,
            ServerPlayer oldTarget,
            ServerPlayer newTarget
    );

}