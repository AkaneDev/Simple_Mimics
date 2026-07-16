package au.akanedev.simplemimics.client;

import au.akanedev.simplemimics.Simplemimics;
import au.akanedev.simplemimics.mimics.client.MimicEntityRenderer;
import au.akanedev.simplemimics.mimics.entity.MimicEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = Simplemimics.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class MimicRendererRegistry {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                MimicEntityType.MIMIC.get(),
                MimicEntityRenderer::new
        );
    }
}