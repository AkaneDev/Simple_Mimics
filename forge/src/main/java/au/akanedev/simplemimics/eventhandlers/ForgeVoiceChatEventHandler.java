package au.akanedev.simplemimics.eventhandlers;

import au.akanedev.simplemimics.manager.MimicManager;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeVoiceChatEventHandler {
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase == TickEvent.Phase.END) {

            VoiceHandler.getInstance()
                    .tick();
            MimicManager.getInstance().onServerTick(event.getServer());
        }
    }
}
