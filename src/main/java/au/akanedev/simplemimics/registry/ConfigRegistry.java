package au.akanedev.simplemimics.registry;

import java.util.HashMap;
import java.util.Map;

public class ConfigRegistry {

    private static final Map<String, ConfigValue<?>> VALUES = new HashMap<>();

    public static void register(ConfigValue<?> value) {
        VALUES.put(value.getName(), value);
    }

    public static ConfigValue<?> get(String name) {
        return VALUES.get(name);
    }

    public static Map<String, ConfigValue<?>> getAll() {
        return VALUES;
    }
}