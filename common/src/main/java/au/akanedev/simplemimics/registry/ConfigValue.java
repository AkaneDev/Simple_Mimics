package au.akanedev.simplemimics.registry;

public abstract class ConfigValue<T> {

    private final String name;
    private final T defaultValue;
    private T value;

    public ConfigValue(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public abstract void setFromString(String input);
}