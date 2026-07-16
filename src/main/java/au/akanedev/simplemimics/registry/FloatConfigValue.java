package au.akanedev.simplemimics.registry;

public class FloatConfigValue extends ConfigValue<Float> {

    public FloatConfigValue(String name, Float defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void setFromString(String input) {
        set(Float.parseFloat(input));
    }
}