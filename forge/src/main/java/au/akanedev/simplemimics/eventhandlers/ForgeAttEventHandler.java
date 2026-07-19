package au.akanedev.simplemimics.eventhandlers;

import au.akanedev.simplemimics.entity.MimicEntity;
import au.akanedev.simplemimics.registry.ForgeEntityRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeAttEventHandler {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ForgeEntityRegistry.MIMIC.get(),
                MimicEntity.createAttributes().build()
        );
    }
}
