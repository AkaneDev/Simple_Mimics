package au.akanedev.simplemimics.mimics.client;

import au.akanedev.simplemimics.Simplemimics;
import au.akanedev.simplemimics.mimics.entity.MimicEntity;
import au.akanedev.simplemimics.mimics.entity.MimicEntityType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Registers the custom entity renderer for MimicEntity
 */
@EventBusSubscriber(modid = Simplemimics.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MimicRendererRegistry {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register our custom renderer for the Mimic entity
        event.registerEntityRenderer(
            MimicEntityType.MIMIC.get(),
                MimicEntityRenderer::new
        );
        
        Simplemimics.LOGGER.info("Registered MimicEntity renderer");
    }
}