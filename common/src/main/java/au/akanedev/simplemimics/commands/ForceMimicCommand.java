package au.akanedev.simplemimics.commands;

import au.akanedev.simplemimics.manager.MimicManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ForceMimicCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("forcemimic")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.argument("position", Vec3Argument.vec3())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {

                                            Vec3 pos = Vec3Argument.getVec3(ctx, "position");
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

                                            var level = ctx.getSource().getLevel();

                                            var mimic = MimicManager.getInstance()
                                                    .spawnMimicForPlayer(target, level);

                                            if (mimic == null) {
                                                ctx.getSource().sendFailure(Component.literal("Failed to spawn mimic."));
                                                return 0;
                                            }

                                            mimic.teleportTo(
                                                    pos.x,
                                                    pos.y,
                                                    pos.z
                                            );

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Spawned mimic of " +
                                                            target.getGameProfile().getName()),
                                                    true
                                            );

                                            return 1;
                                        })
                                )
                        )
        );
    }
}