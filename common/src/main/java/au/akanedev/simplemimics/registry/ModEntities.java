package au.akanedev.simplemimics.registry;

public class ModEntities {

    private static PlatformEntityRegistry registry;

    public static void init(PlatformEntityRegistry platformRegistry) {
        registry = platformRegistry;
    }

    public static PlatformEntityRegistry get() {
        if (registry == null) {
            throw new IllegalStateException(
                    "PlatformEntityRegistry has not been initialised"
            );
        }

        return registry;
    }
}