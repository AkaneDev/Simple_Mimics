package au.akanedev.simplemimics.client;

import au.akanedev.simplemimics.mimics.client.MimicEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static au.akanedev.simplemimics.Simplemimics.MODID;
import static au.akanedev.simplemimics.mimics.entity.MimicEntityType.MIMIC;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                MIMIC.get(),
                MimicEntityRenderer::new
        );
    }
}