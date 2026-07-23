package au.akanedev.simplemimics;

import au.akanedev.simplemimics.registry.BooleanConfigValue;
import au.akanedev.simplemimics.registry.ConfigRegistry;
import au.akanedev.simplemimics.registry.FloatConfigValue;

public class CustomConfigs {
    public CustomConfigs() {
        ConfigRegistry.register(new FloatConfigValue("CHANCE_TO_SPEAK", 0.003f));
        ConfigRegistry.register(new BooleanConfigValue("ENABLE_ADDON_JUMPSCARES", false));
//        ConfigRegistry.register(new BooleanConfigValue("DEVMODE", false));
    }
}
