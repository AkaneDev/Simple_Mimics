package au.akanedev.simplemimics;

import au.akanedev.simplemimics.commands.*;
import au.akanedev.simplemimics.voice.VoiceClipsCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgeModCommands {

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