package au.akanedev.simplemimics;

import au.akanedev.simplemimics.mimics.entity.MimicEntityType;
import au.akanedev.simplemimics.mimics.manager.MimicManager;
import au.akanedev.simplemimics.mimics.voice.VoiceChatEventHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Simplemimics.MODID)
public class Simplemimics {

    public static final String MODID = "simplemimics";
    private static MinecraftServer server;
    public static final Logger LOGGER = LogUtils.getLogger();

    public Simplemimics(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        bus.addListener(this::commonSetup);
        MimicEntityType.ENTITY_TYPES.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new VoiceChatEventHandler());
        new CustomConfigs();
    }

    public static boolean isDevMode() {
        return false;
    }

    public static void BroadcastMessage(String s, String subsystem) {
        for (ServerPlayer player: server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal("[DevMode +" + subsystem + "+]: " + s));
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }

    /**
     * Handle server tick for mimic AI
     */
    @SubscribeEvent
    public void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            if (event.getServer() != null && event.getServer().getLevel(Level.OVERWORLD) != null) {
                MimicManager.getInstance().onServerTick(event.getServer().getLevel(Level.OVERWORLD));
            }
        }
    }

    /**
     * When a player logs in, they might become a target
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isDevMode()) {
                player.sendSystemMessage(Component.literal("[DevMode]: Developer Version of the mod enabled, there will be alot of debug spew in the chat"));
            }
            LOGGER.info("Player {} joined - they may become a mimic target", player.getGameProfile().getName());
        }
    }
    /**
     * When a player logs off, remove any mimics targeting them
     */
    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LOGGER.info("Player {} left - removing their mimics", player.getGameProfile().getName());
            MimicManager.getInstance().removeMimicsForPlayer(player.getUUID());
        }
    }
    /**
     * When server stops, clear all mimics
     */
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        LOGGER.info("Server stopped - clearing all mimics");
        MimicManager.getInstance().clearAll();
    }
}
