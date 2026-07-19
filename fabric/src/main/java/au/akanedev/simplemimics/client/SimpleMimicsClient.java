package au.akanedev.simplemimics.client;

import au.akanedev.simplemimics.registry.FabricEntityRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class SimpleMimicsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(
                FabricEntityRegistry.MIMIC,
                MimicEntityRenderer::new
        );

    }
}