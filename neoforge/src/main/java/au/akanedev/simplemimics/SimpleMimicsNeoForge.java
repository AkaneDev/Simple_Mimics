package au.akanedev.simplemimics;


import au.akanedev.simplemimics.events.NeoForgeVoiceChatEventHandler;
import au.akanedev.simplemimics.registry.ModEntities;
import au.akanedev.simplemimics.registry.NeoForgeAttributes;
import au.akanedev.simplemimics.registry.NeoForgeEntityRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class SimpleMimicsNeoForge {

    public SimpleMimicsNeoForge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        SimpleMimicsCommon.init();
        eventBus.register(NeoForgeAttributes.class);
        NeoForgeEntityRegistry.ENTITY_TYPES.register(eventBus);
        NeoForge.EVENT_BUS.register(NeoForgeModCommands.class);

        ModEntities.init(
                new NeoForgeEntityRegistry()
        );

    }
}