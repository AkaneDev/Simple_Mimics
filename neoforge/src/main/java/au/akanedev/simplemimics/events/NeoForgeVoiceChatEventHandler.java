package au.akanedev.simplemimics.events;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.SimpleMimicsNeoForge;
import au.akanedev.simplemimics.manager.MimicManager;
import au.akanedev.simplemimics.util.PlayerData;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.UUID;

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
    public static void onMessageReceived(ServerChatEvent event) {
        PlayerData data = new PlayerData(
                event.getPlayer().getUUID(),
                event.getPlayer()
        );

        SimpleMimicsNeoForge.chatManager.addMessage(
                data,
                event.getMessage()
        );
    }
}