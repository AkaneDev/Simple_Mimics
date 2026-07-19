package au.akanedev.simplemimics.registry;

public class StringConfigValue extends ConfigValue<String> {

    public StringConfigValue(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void setFromString(String input) {
        set(String.copyValueOf(input.toCharArray()));
    }
}