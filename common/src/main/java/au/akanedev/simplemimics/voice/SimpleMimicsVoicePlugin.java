package au.akanedev.simplemimics.voice;

import au.akanedev.simplemimics.Constants;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.minecraft.server.level.ServerPlayer;

public class SimpleMimicsVoicePlugin implements VoicechatPlugin {

    private static final String ID = "simplemimics";

    @Override
    public String getPluginId() {
        return ID;
    }

    @Override
    public void initialize(VoicechatApi api) {

        if (api instanceof VoicechatServerApi serverApi) {
            VoiceHandler.getInstance().init(serverApi);
        }

        Constants.LOG.info("[SimpleMimics] Voice chat initialised");
    }


    @Override
    public void registerEvents(EventRegistration registration) {

        registration.registerEvent(
                MicrophonePacketEvent.class,
                this::onMicrophonePacket
        );
    }


    private void onMicrophonePacket(MicrophonePacketEvent event) {

        var connection = event.getSenderConnection();

        if (connection == null || connection.getPlayer() == null)
            return;


        Object playerObject =
                connection.getPlayer().getPlayer();


        if (!(playerObject instanceof ServerPlayer player))
            return;


        MicrophonePacket packet =
                event.getPacket();


        if (packet == null)
            return;

        VoiceHandler.getInstance()
                .onAudioPacket(player, packet);
    }
}