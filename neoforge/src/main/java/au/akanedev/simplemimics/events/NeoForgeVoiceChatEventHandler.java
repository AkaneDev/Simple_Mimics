package au.akanedev.simplemimics.events;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.SimpleMimicsNeoForge;
import au.akanedev.simplemimics.manager.MimicManager;
import au.akanedev.simplemimics.util.PlayerData;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeVoiceChatEventHandler {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        VoiceHandler.getInstance()
                    .tick();
        MimicManager.getInstance().onServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PlayerDataUtils.setServerRef(event.getServer());
    }

    @SubscribeEvent
    public static void onMessageReceived(ClientChatReceivedEvent event) {
        if (!event.isSystem()) {
            PlayerData data = new PlayerData(event.getSender());
            SimpleMimicsNeoForge.chatManager.addMessage(data, event.getMessage());
        }
    }
}
