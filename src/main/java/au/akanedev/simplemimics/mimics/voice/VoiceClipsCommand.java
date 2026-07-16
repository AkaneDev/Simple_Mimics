package au.akanedev.simplemimics.mimics.voice;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.api.Entity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class VoiceClipsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("voiceclips")
                        .requires(source -> source.hasPermission(2))

                        // =====================================================
                        // LIST ALL PLAYERS WITH CLIPS
                        // =====================================================
                        .then(Commands.literal("list")
                                .executes(ctx -> {

                                    VoiceHandler handler = VoiceHandler.getInstance();

                                    Map<UUID, List<List<VoiceHandler.TimedPacket>>> data =
                                            handler.getRecordedClips(); // FIXED METHOD

                                    if (data.isEmpty()) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("No voice clips recorded."),
                                                false
                                        );
                                        return 1;
                                    }

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Stored voice clips:")
                                                    .withStyle(ChatFormatting.GOLD),
                                            false
                                    );

                                    for (Map.Entry<UUID, List<List<VoiceHandler.TimedPacket>>> entry : data.entrySet()) {

                                        UUID uuid = entry.getKey();
                                        int clipCount = entry.getValue().size();

                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(uuid + " -> " + clipCount + " clips"),
                                                false
                                        );
                                    }

                                    return 1;
                                })
                        )

                        // =====================================================
                        // PLAY RANDOM CLIP (ANY PLAYER)
                        // =====================================================
                        .then(Commands.literal("playrandom")
                                .executes(ctx -> {

                                    ServerPlayer caller = ctx.getSource().getPlayer();
                                    if (caller == null) return 0;

                                    VoiceHandler handler = VoiceHandler.getInstance();

                                    UUID randomOwner = handler.getRandomPlayerWithClips();

                                    if (randomOwner == null) {
                                        ctx.getSource().sendFailure(
                                                Component.literal("No clips available.")
                                        );
                                        return 0;
                                    }

                                    handler.FreplayVoice(
                                            caller,
                                            randomOwner
                                    );

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Playing random stored clip."),
                                            false
                                    );

                                    return 1;
                                })
                        )

                        // =====================================================
                        // PLAY CLIP FROM SPECIFIC PLAYER
                        // =====================================================
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {

                                    ServerPlayer caller =
                                            ctx.getSource().getPlayer();

                                    ServerPlayer target =
                                            EntityArgument.getPlayer(ctx, "player");

                                    if (caller == null) return 0;

                                    VoiceHandler.getInstance()
                                            .FreplayVoice(
                                                    caller,
                                                    target.getUUID()
                                            );

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Playing clip from " +
                                                            target.getName().getString()
                                            ),
                                            false
                                    );

                                    return 1;
                                })
                        )
        );
    }
}