package au.akanedev.simplemimics.mimics.voice;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.minecraft.server.level.ServerPlayer;

@ForgeVoicechatPlugin
public class VoiceChatPlugin implements VoicechatPlugin {

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

        System.out.println("[SCPVoice] Initialised");
    }


    @Override
    public void registerEvents(EventRegistration registration) {

        registration.registerEvent(
                MicrophonePacketEvent.class,
                this::onMicrophonePacket
        );

        System.out.println("[SCPVoice] Registered microphone listener");
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