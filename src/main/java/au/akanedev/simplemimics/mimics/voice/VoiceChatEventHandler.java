package au.akanedev.simplemimics.mimics.voice;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VoiceChatEventHandler {
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase == TickEvent.Phase.END) {

            VoiceHandler.getInstance()
                    .tick();

        }
    }
}
