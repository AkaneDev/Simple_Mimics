package au.akanedev.simplemimics.client;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.registry.NeoForgeEntityRegistry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = Constants.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class NeoForgeMimicRendererRegistry {
    static {
        Constants.LOG.info("NeoForgeMimicRendererRegistry loaded");
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(
                NeoForgeEntityRegistry.MIMIC.get(),
                MimicEntityRenderer::new
        );

        Constants.LOG.info("Registered Mimic renderer");
    }
}