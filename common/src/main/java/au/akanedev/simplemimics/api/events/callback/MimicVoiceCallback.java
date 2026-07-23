package au.akanedev.simplemimics.api.events.callback;

import au.akanedev.simplemimics.entity.MimicEntity;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MimicVoiceCallback {

    /**
     * Return false to cancel voice playback
     */
    boolean onVoice(
            MimicEntity mimic,
            ServerPlayer copiedPlayer,
            ServerPlayer targetPlayer
    );

}