package au.akanedev.simplemimics.client;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.registry.ForgeEntityRegistry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = Constants.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ForgeMimicRendererRegistry {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(
                ForgeEntityRegistry.MIMIC.get(),
                MimicEntityRenderer::new
        );
    }
}