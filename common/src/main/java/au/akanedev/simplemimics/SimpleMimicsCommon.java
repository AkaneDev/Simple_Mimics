package au.akanedev.simplemimics;

import au.akanedev.simplemimics.platform.Services;
import au.akanedev.simplemimics.voice.VoiceHandler;

public class SimpleMimicsCommon {
    public static void init() {
        new CustomConfigs();
        Constants.LOG.info("SimpleMimicsCommon has been initialized!");
        // Log What env it is and modloader
        Constants.LOG.info("Your Using Modloader: " + Services.PLATFORM.getPlatformName() + ", and is in an " + Services.PLATFORM.getEnvironmentName() + " Enviroment");
    }
}
