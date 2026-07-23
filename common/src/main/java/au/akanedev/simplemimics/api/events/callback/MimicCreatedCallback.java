package au.akanedev.simplemimics.api.events.callback;

import au.akanedev.simplemimics.entity.MimicEntity;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MimicCreatedCallback {

    void onCreated(
            MimicEntity mimic,
            ServerPlayer copiedPlayer
    );

}