package au.akanedev.simplemimics.commands;

import au.akanedev.simplemimics.manager.ChatManager;
import au.akanedev.simplemimics.util.PlayerData;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Deque;
import java.util.Map;

public class ChatManagerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("chatmanager")
                        .requires(source -> source.hasPermission(2))

                        // =====================================================
                        // LIST ALL PLAYERS WITH STORED MESSAGES
                        // =====================================================
                        .then(Commands.literal("list")
                                .executes(ctx -> {

                                    ChatManager manager =
                                            ChatManager.getInstance();

                                    Map<PlayerData, Deque<Component>> data =
                                            manager.getMessagesToMimic();

                                    if (data.isEmpty()) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "No chat messages stored."
                                                ),
                                                false
                                        );
                                        return 1;
                                    }

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Stored chat messages:"
                                            ).withStyle(ChatFormatting.GOLD),
                                            false
                                    );

                                    for (Map.Entry<PlayerData, Deque<Component>> entry
                                            : data.entrySet()) {

                                        PlayerData playerData = entry.getKey();
                                        int messageCount = entry.getValue().size();

                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        playerData.getPlayer().getName().getString() + " -> "
                                                                + messageCount
                                                                + " messages"
                                                ),
                                                false
                                        );
                                    }

                                    return 1;
                                })
                        )

                        // =====================================================
                        // GET RANDOM MESSAGE FROM ANY PLAYER
                        // =====================================================
                        .then(Commands.literal("random")
                                .executes(ctx -> {

                                    ChatManager manager =
                                            ChatManager.getInstance();

                                    Map<PlayerData, Deque<Component>> data =
                                            manager.getMessagesToMimic();

                                    if (data.isEmpty()) {
                                        ctx.getSource().sendFailure(
                                                Component.literal(
                                                        "No chat messages available."
                                                )
                                        );
                                        return 0;
                                    }

                                    PlayerData[] players =
                                            data.keySet().toArray(new PlayerData[0]);

                                    PlayerData randomPlayer =
                                            players[(int) (Math.random() * players.length)];

                                    Component message =
                                            manager.getRandomMessage(randomPlayer);

                                    if (message == null) {
                                        ctx.getSource().sendFailure(
                                                Component.literal(
                                                        "No messages available."
                                                )
                                        );
                                        return 0;
                                    }

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Random message: "
                                            ).append(message),
                                            false
                                    );

                                    return 1;
                                })

                                // =================================================
                                // GET RANDOM MESSAGE FROM SPECIFIC PLAYER
                                // =================================================
                                .then(Commands.argument(
                                                        "player",
                                                        EntityArgument.player()
                                                )
                                                .executes(ctx -> {

                                                    ServerPlayer target =
                                                            EntityArgument.getPlayer(
                                                                    ctx,
                                                                    "player"
                                                            );

                                                    PlayerData playerData =
                                                            PlayerDataUtils.getPlayerData(
                                                                    target
                                                            );

                                                    if (playerData == null) {
                                                        ctx.getSource().sendFailure(
                                                                Component.literal(
                                                                        "No PlayerData found for "
                                                                                + target.getName().getString()
                                                                )
                                                        );
                                                        return 0;
                                                    }

                                                    Component message =
                                                            ChatManager.getInstance()
                                                                    .getRandomMessage(
                                                                            playerData
                                                                    );

                                                    if (message == null) {
                                                        ctx.getSource().sendFailure(
                                                                Component.literal(
                                                                        "No stored messages for "
                                                                                + target.getName().getString()
                                                                )
                                                        );
                                                        return 0;
                                                    }

                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal(
                                                                    "Random message from "
                                                            ).append(
                                                                    target.getName()
                                                            ).append(
                                                                    ": "
                                                            ).append(
                                                                    message
                                                            ),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                )
                        )
        );
    }
}