package au.akanedev.simplemimics;

import au.akanedev.simplemimics.commands.ConfigCommand;
import au.akanedev.simplemimics.commands.ForceMimicCommand;
import au.akanedev.simplemimics.voice.VoiceClipsCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;


public class NeoForgeModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ForceMimicCommand.register(dispatcher);
        ConfigCommand.register(dispatcher);
        VoiceClipsCommand.register(dispatcher);
    }
}