package au.akanedev.simplemimics.registry;

import au.akanedev.simplemimics.util.PlayerDataUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class CommandConfigValue extends ConfigValue<String> {
    public CommandConfigValue(String name, String defaultValue) {super(name, defaultValue);}

    @Override
    public void setFromString(String input) {
        if (isSenderPermitted(getCtx(), 4)) {
            MinecraftServer server = PlayerDataUtils.getServer();
            if(server == null) return;
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    getDefault()
            );
        }
        else  {
            getCtx().getSource().sendFailure(
                    Component.literal(
                            "This isn't a normal configuration value. " +
                                    "It executes a server command and requires Level 4 permissions."
                    )
            );
        }
    }

    @Override
    public boolean isComment() {
        return true;
    }
}
