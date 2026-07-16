package au.akanedev.simplemimics.commands;

import au.akanedev.simplemimics.registry.ConfigRegistry;
import au.akanedev.simplemimics.registry.ConfigValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("mimicConfig")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.argument(
                                                "config",
                                                StringArgumentType.word()
                                        )
                                        .suggests((context, builder) -> {

                                            for (String name : ConfigRegistry.getAll().keySet()) {
                                                builder.suggest(name);
                                            }

                                            return builder.buildFuture();

                                        })

                                        .then(Commands.argument(
                                                        "value",
                                                        StringArgumentType.greedyString()
                                                )
                                                .executes(context -> {

                                                    String configName =
                                                            StringArgumentType.getString(context, "config");

                                                    String value =
                                                            StringArgumentType.getString(context, "value");


                                                    ConfigValue<?> config =
                                                            ConfigRegistry.get(configName);


                                                    if (config == null) {
                                                        context.getSource().sendFailure(
                                                                net.minecraft.network.chat.Component.literal(
                                                                        "Unknown config: " + configName
                                                                )
                                                        );

                                                        return 0;
                                                    }


                                                    try {
                                                        config.setFromString(value);

                                                        context.getSource().sendSuccess(
                                                                () -> net.minecraft.network.chat.Component.literal(
                                                                        "Set " + configName + " to " + value
                                                                ),
                                                                true
                                                        );

                                                    } catch (Exception e) {

                                                        context.getSource().sendFailure(
                                                                net.minecraft.network.chat.Component.literal(
                                                                        "Invalid value for " + configName
                                                                )
                                                        );

                                                        return 0;
                                                    }

                                                    return 1;
                                                }))
                        )
        );
    }
}