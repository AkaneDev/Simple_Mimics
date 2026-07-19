package au.akanedev.simplemimics;

import au.akanedev.simplemimics.commands.ConfigCommand;
import au.akanedev.simplemimics.commands.ForceMimicCommand;
import au.akanedev.simplemimics.voice.VoiceClipsCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;

public class FabricModCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            register(dispatcher);
        });
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ForceMimicCommand.register(dispatcher);
        ConfigCommand.register(dispatcher);
        VoiceClipsCommand.register(dispatcher);
    }
}