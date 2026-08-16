package au.akanedev.simplemimics.registry;

import au.akanedev.simplemimics.Constants;
import au.akanedev.simplemimics.util.PlayerDataUtils;

public class CommentConfigValue extends ConfigValue<String> {
    public CommentConfigValue(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void setFromString(String input) {
        Constants.LOG.info(getDefault());
        PlayerDataUtils.sendGlobalMessage(getDefault());
    }

    @Override
    public boolean isComment() {
        return true;
    }

}
