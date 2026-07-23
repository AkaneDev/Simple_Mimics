package au.akanedev.simplemimics.api.events.callback;

import au.akanedev.simplemimics.entity.MimicEntity;

@FunctionalInterface
public interface MimicRemovedCallback {

    void onRemoved(
            MimicEntity mimic
    );

}