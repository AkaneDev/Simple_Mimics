package au.akanedev.simplemimics.registry;

public class LongConfigValue extends ConfigValue<Long> {

    public LongConfigValue(String name, Long defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void setFromString(String input) {
        set(Long.parseLong(input));
    }
}