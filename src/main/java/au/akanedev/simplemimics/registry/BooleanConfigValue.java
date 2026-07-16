package au.akanedev.simplemimics.registry;

public class BooleanConfigValue extends ConfigValue<Boolean> {

    public BooleanConfigValue(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void setFromString(String input) {
        set(Boolean.parseBoolean(input));
    }
}