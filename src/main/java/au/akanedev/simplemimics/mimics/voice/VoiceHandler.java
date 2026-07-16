package au.akanedev.simplemimics.mimics.voice;

import au.akanedev.simplemimics.mimics.entity.MimicEntity;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.*;

public class VoiceHandler {

    private static final VoiceHandler INSTANCE = new VoiceHandler();


    private VoicechatServerApi api;


    private final ScheduledExecutorService voiceScheduler =
            Executors.newSingleThreadScheduledExecutor();



    /*
     * Recording
     */

    private final Map<UUID, List<TimedPacket>> liveBuffers =
            new ConcurrentHashMap<>();


    private final Map<UUID, List<List<TimedPacket>>> recordedClips =
            new ConcurrentHashMap<>();


    private final Map<UUID, Long> lastPacketTime =
            new ConcurrentHashMap<>();


    private final Map<UUID, Long> clipStartTime =
            new ConcurrentHashMap<>();




    /*
     * Playback queues
     */

    private final Map<UUID, MimicPlayback> mimicQueues =
            new ConcurrentHashMap<>();


    private final Map<UUID, PlayerPlayback> playerQueues =
            new ConcurrentHashMap<>();




    private static final long SILENCE_GAP_MS = 2000;
    private static final long MIN_CLIP_SIZE_MS = 1000;

    private static final int MAX_CLIPS_PER_PLAYER = 20;



    public static VoiceHandler getInstance() {
        return INSTANCE;
    }



    public void init(VoicechatServerApi api) {

        this.api = api;


        voiceScheduler.scheduleAtFixedRate(
                this::tickPlayback,
                0, 1,
                TimeUnit.MILLISECONDS
        );
    }



    public VoicechatServerApi getApi() {
        return api;
    }




    /*
     * RECORDING
     */


    public void onAudioPacket(
            ServerPlayer player,
            MicrophonePacket packet
    ) {

        UUID id = player.getUUID();

        long now = System.currentTimeMillis();



        List<TimedPacket> buffer =
                liveBuffers.computeIfAbsent(
                        id,
                        k -> Collections.synchronizedList(
                                new ArrayList<>()
                        )
                );



        if (!clipStartTime.containsKey(id)) {

            clipStartTime.put(
                    id,
                    now
            );
        }



        lastPacketTime.put(
                id,
                now
        );



        buffer.add(
                new TimedPacket(
                        packet,
                        now
                )
        );
    }




    private void flushClip(UUID id) {

        List<TimedPacket> buffer =
                liveBuffers.get(id);



        if (buffer == null || buffer.isEmpty())
            return;



        List<TimedPacket> clip =
                new ArrayList<>(buffer);



        List<List<TimedPacket>> clips =
                recordedClips.computeIfAbsent(
                        id,
                        k -> new ArrayList<>()
                );



        clips.add(clip);



        if (clips.size() > MAX_CLIPS_PER_PLAYER) {
            clips.remove(0);
        }



        buffer.clear();
    }





    private void tickRecording() {

        long now = System.currentTimeMillis();



        for (UUID id :
                new ArrayList<>(lastPacketTime.keySet())) {



            long last =
                    lastPacketTime.getOrDefault(
                            id,
                            now
                    );



            if (now - last < SILENCE_GAP_MS)
                continue;



            List<TimedPacket> buffer =
                    liveBuffers.get(id);



            if (buffer != null && !buffer.isEmpty()) {


                long start =
                        clipStartTime.getOrDefault(
                                id,
                                now
                        );



                if (now - start >= MIN_CLIP_SIZE_MS) {

                    flushClip(id);

                } else {

                    buffer.clear();

                }
            }



            clipStartTime.remove(id);
            lastPacketTime.remove(id);
        }
    }




    /*
     * CLIP ACCESS
     */


    public List<TimedPacket> getRandomClip(UUID player) {

        List<List<TimedPacket>> clips =
                recordedClips.get(player);



        if (clips == null || clips.isEmpty())
            return null;



        return clips.get(
                new Random().nextInt(
                        clips.size()
                )
        );
    }



    public UUID getRandomPlayerWithClips() {

        if (recordedClips.isEmpty())
            return null;



        List<UUID> players =
                new ArrayList<>(
                        recordedClips.keySet()
                );



        return players.get(
                new Random().nextInt(
                        players.size()
                )
        );
    }




    /*
     * MIMIC
     */


    public void replayVoice(
            MimicEntity mimic,
            UUID target
    ) {


        List<TimedPacket> clip =
                getRandomClip(target);



        if (clip == null)
            return;



        MimicPlayback playback =
                mimicQueues.computeIfAbsent(
                        mimic.getUUID(),
                        id -> new MimicPlayback(
                                mimic.getVoiceChannel()
                        )
                );



        if (playback.queue.isEmpty()) {

            playback.nextSend =
                    System.currentTimeMillis()
                            + 100;
        }



        playback.queue.addAll(clip);
    }





    /*
     * COMMAND
     */


    public void FreplayVoice(
            ServerPlayer caller,
            UUID voice
    ) {


        if (api == null)
            return;



        List<TimedPacket> clip =
                getRandomClip(voice);



        if (clip == null)
            return;



        VoicechatConnection connection =
                api.getConnectionOf(
                        caller.getUUID()
                );



        if (connection == null)
            return;



        PlayerPlayback playback =
                playerQueues.computeIfAbsent(
                        caller.getUUID(),
                        id -> new PlayerPlayback(
                                connection
                        )
                );



        if (playback.queue.isEmpty()) {

            playback.nextSend =
                    System.currentTimeMillis()
                            + 200;
        }



        playback.queue.addAll(clip);
    }





    /*
     * AUDIO PLAYBACK
     */


    private void tickPlayback() {

        long now =
                System.currentTimeMillis();



        for (Iterator<Map.Entry<UUID, MimicPlayback>> it =
             mimicQueues.entrySet().iterator();
             it.hasNext();) {


            MimicPlayback playback =
                    it.next().getValue();



            if (now < playback.nextSend)
                continue;



            TimedPacket packet =
                    playback.queue.poll();



            if (packet == null) {

                it.remove();
                continue;
            }



            if (playback.channel != null) {

                playback.channel.send(
                        packet.packet()
                );
            }



            if (!playback.queue.isEmpty()) {

                TimedPacket next =
                        playback.queue.peek();



                playback.nextSend =
                        now + Math.max(
                                1,
                                next.time()
                                        -
                                        packet.time()
                        );

            } else {

                playback.nextSend =
                        now + 20;
            }
        }




        for (Iterator<Map.Entry<UUID, PlayerPlayback>> it =
             playerQueues.entrySet().iterator();
             it.hasNext();) {


            PlayerPlayback playback =
                    it.next().getValue();



            if (now < playback.nextSend)
                continue;



            TimedPacket packet =
                    playback.queue.poll();



            if (packet == null) {

                it.remove();
                continue;
            }



            StaticSoundPacket sound =
                    packet.packet()
                            .staticSoundPacketBuilder()
                            .build();



            if (sound != null && api != null) {

                api.sendStaticSoundPacketTo(
                        playback.connection,
                        sound
                );
            }



            if (!playback.queue.isEmpty()) {

                TimedPacket next =
                        playback.queue.peek();



                playback.nextSend =
                        now + Math.max(
                                1,
                                next.time()
                                        -
                                        packet.time()
                        );

            } else {

                playback.nextSend =
                        now + 20;
            }
        }
    }





    /*
     * Called by Forge server tick
     */


    public void tick() {

        tickRecording();

    }





    public Map<UUID, List<List<TimedPacket>>> getRecordedClips() {
        return recordedClips;
    }





    private static class MimicPlayback {

        final Queue<TimedPacket> queue =
                new ConcurrentLinkedQueue<>();


        final EntityAudioChannel channel;


        long nextSend;


        MimicPlayback(EntityAudioChannel channel) {
            this.channel = channel;
        }
    }





    private static class PlayerPlayback {

        final Queue<TimedPacket> queue =
                new ConcurrentLinkedQueue<>();


        final VoicechatConnection connection;


        long nextSend;


        PlayerPlayback(
                VoicechatConnection connection
        ) {
            this.connection = connection;
        }
    }




    public record TimedPacket(
            MicrophonePacket packet,
            long time
    ) {}
}