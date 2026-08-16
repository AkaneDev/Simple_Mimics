package au.akanedev.simplemimics.registry;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;


public abstract class ConfigValue<T> {

    private final String name;
    private final T defaultValue;
    private T value;
    private CommandContext<CommandSourceStack> ctx;

    public ConfigValue(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean isComment() {
        return false;
    }

    public boolean isSenderPermitted(CommandContext<CommandSourceStack> ctx, int PermissionLevel) {
        return ctx.getSource().hasPermission(PermissionLevel);
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

    public CommandContext<CommandSourceStack> getCtx() {
        return ctx;
    }

    public void setCtx(CommandContext<CommandSourceStack> ctx) {
        this.ctx = ctx;
    }
}