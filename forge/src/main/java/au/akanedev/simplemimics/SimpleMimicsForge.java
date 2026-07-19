package au.akanedev.simplemimics;

import au.akanedev.simplemimics.eventhandlers.ForgeAttEventHandler;
import au.akanedev.simplemimics.registry.ForgeEntityRegistry;
import au.akanedev.simplemimics.registry.ModEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class SimpleMimicsForge {

    public SimpleMimicsForge(FMLJavaModLoadingContext context) {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");
        SimpleMimicsCommon.init();
        IEventBus bus = context.getModEventBus();
        bus.register(this);
        bus.register(ForgeAttEventHandler.class);
        ForgeEntityRegistry.ENTITY_TYPES.register(bus);

        ModEntities.init(
                new ForgeEntityRegistry()
        );
    }
}