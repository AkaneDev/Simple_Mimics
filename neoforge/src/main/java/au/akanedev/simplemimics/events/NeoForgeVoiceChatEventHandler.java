package au.akanedev.simplemimics.events;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.manager.MimicManager;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeVoiceChatEventHandler {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        VoiceHandler.getInstance()
                    .tick();
        MimicManager.getInstance().onServerTick(event.getServer());
    }
}
