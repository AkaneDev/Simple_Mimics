package au.akanedev.simplemimics.registry;

public class IntConfigValue extends ConfigValue<Integer> {

    public IntConfigValue(String name, Integer defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void setFromString(String input) {
        set(Integer.parseInt(input));
    }
}