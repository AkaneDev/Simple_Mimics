package au.akanedev.simplemimics.manager;

import au.akanedev.simplemimics.util.PlayerData;
import au.akanedev.simplemimics.util.PlayerDataUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {
    static ChatManager INSTANCE;
    public Map<PlayerData, Deque<Component>> MessagesToMimic = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES = 20;
    private static Random random = new Random();

    public ChatManager() {
        if (INSTANCE == null) {
            INSTANCE = this;
        }
    }

    public static ChatManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatManager();
        }
        return INSTANCE;
    }

    public void addMessage(PlayerData playerData, Component message) {
        Deque<Component> messages = MessagesToMimic.computeIfAbsent(
                playerData,
                p -> new ArrayDeque<>()
        );

        messages.addLast(message);

        while (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
    }

    public Component getRandomMessage(PlayerData playerData) {
        List<Component> messagesList = MessagesToMimic.get(playerData).stream().toList();
        if (messagesList.isEmpty()) {
            return null;
        }
        return messagesList.get(random.nextInt(messagesList.size()));
    }
}
