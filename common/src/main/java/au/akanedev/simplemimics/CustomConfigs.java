package au.akanedev.simplemimics;

import au.akanedev.simplemimics.registry.*;

public class CustomConfigs {
    public CustomConfigs() {
        // Such a hack to show a warning I love it so much
        // Added at the top so these show first
        ConfigRegistry.register(new CommentConfigValue("DevModeWillSpamYourChatSetToAnythingForMoreInfo", "Im not Joking, This could send 20 messages a second, it has crashed me before - AkaneDev"));
        ConfigRegistry.register(new CommentConfigValue("WarningAboutMimicLimitsSetToAnythingForMoreInfo", "The Mimics have a limit of 10 mimics alive total and of 1 per player however you can set this to any limits just... careful 50 might lag the game"));
        // end hacky stuff
        // start super hacky stuff like log4shell level of hacky
        ConfigRegistry.register(new CommandConfigValue("KillAllMimics", "kill @e[type=simplemimics:mimic]"));
        // end super hackystuff
        ConfigRegistry.register(new FloatConfigValue("CHANCE_TO_SPEAK", 0.003f));
        ConfigRegistry.register(new BooleanConfigValue("ENABLE_ADDON_JUMPSCARES", false));
        ConfigRegistry.register(new BooleanConfigValue("DEVMODE", false));
        ConfigRegistry.register(new IntConfigValue("MAX_TOTAL_MIMICS", 10));
        ConfigRegistry.register(new IntConfigValue("MAX_MIMICS_PER_PLAYER", 1));
        ConfigRegistry.register(new FloatConfigValue("CHANCE_TO_TEXT", 0.003f));
    }
}

