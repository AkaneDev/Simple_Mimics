package au.akanedev.simplemimics.api.events.callback;

import au.akanedev.simplemimics.entity.MimicEntity;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MimicActionCallback {

    void onAction(ServerPlayer player, MimicEntity mimicEntity);

}