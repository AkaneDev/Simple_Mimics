package au.akanedev.simplemimics.eventhandlers;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.manager.MimicManager;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ForgeVoiceChatEventHandler {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase == TickEvent.Phase.END) {

            VoiceHandler.getInstance()
                    .tick();
            MimicManager.getInstance().onServerTick(event.getServer());
        }
    }
    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        PlayerDataUtils.setServerRef(event.getServer());
    }
}
