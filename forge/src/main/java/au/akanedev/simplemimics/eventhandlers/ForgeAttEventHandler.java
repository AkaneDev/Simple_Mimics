package au.akanedev.simplemimics.eventhandlers;

import au.akanedev.simplemimics.entity.MimicEntity;
import au.akanedev.simplemimics.registry.ForgeEntityRegistry;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
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
