package au.akanedev.simplemimics.eventhandlers;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.SimpleMimicsForge;
import au.akanedev.simplemimics.manager.MimicManager;
import au.akanedev.simplemimics.util.PlayerData;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

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
    @SubscribeEvent
    public static void onMessageReceived(ServerChatEvent event) {
        PlayerData data = new PlayerData(
                event.getPlayer().getUUID(),
                event.getPlayer()
        );

        SimpleMimicsForge.chatManager.addMessage(
                data,
                event.getMessage()
        );
    }
}
