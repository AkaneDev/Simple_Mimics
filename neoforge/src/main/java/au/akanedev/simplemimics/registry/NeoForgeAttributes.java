package au.akanedev.simplemimics.registry;

import au.akanedev.simplemimics.entity.MimicEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class NeoForgeAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {

        event.put(
                NeoForgeEntityRegistry.MIMIC.get(),
                MimicEntity.createAttributes().build()
        );

    }
}