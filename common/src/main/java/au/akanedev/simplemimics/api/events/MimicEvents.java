package au.akanedev.simplemimics.api.events;

import au.akanedev.simplemimics.api.events.callback.*;

public final class MimicEvents {

    private MimicEvents() {}

    /**
     * Fired when a mimic is created
     */
    public static final SimpleEvent<MimicCreatedCallback> CREATED =
            new SimpleEvent<>();

    /**
     * Fired when a mimic is removed
     */
    public static final SimpleEvent<MimicRemovedCallback> REMOVED =
            new SimpleEvent<>();

    /**
     * Fired before a mimic plays a voice clip
     */
    public static final SimpleEvent<MimicVoiceCallback> VOICE =
            new SimpleEvent<>();

    /**
     * Fired when a mimic changes Target B
     */
    public static final SimpleEvent<MimicTargetChangedCallback> TARGET_CHANGED =
            new SimpleEvent<>();

    /**
     * Fired in the manager tick loop for people to add their own actions
     */
    public static final SimpleEvent<MimicActionCallback>  ACTION =
            new SimpleEvent<>();

    /**
     * Fired in the Movement for the mimics for people to adjust the mimic's movement
     */
    public static final SimpleEvent<MimicMovementCallback> MOVEMENT =
            new SimpleEvent<>();

    public static final SimpleEvent<MimicJumpscareCallback> JUMPSCARE_CALLBACK =
            new SimpleEvent<>();

}