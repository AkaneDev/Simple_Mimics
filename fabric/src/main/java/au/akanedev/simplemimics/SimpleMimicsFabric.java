package au.akanedev.simplemimics;

import au.akanedev.simplemimics.manager.MimicManager;
import au.akanedev.simplemimics.registry.FabricEntityRegistry;
import au.akanedev.simplemimics.registry.ModEntities;
import au.akanedev.simplemimics.voice.VoiceHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class SimpleMimicsFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        SimpleMimicsCommon.init();
        // Force class loading so registry happens
        FabricEntityRegistry.MIMIC.toString();
        FabricEntityRegistry.registerAttributes();
        FabricModCommands.init();

        ModEntities.init(
                new FabricEntityRegistry()
        );
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MimicManager.getInstance().onServerTick(server);
            VoiceHandler.getInstance().tick();
        });
    }
}
