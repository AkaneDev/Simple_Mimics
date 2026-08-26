package au.akanedev.simplemimics.manager;

import au.akanedev.simplemimics.util.PlayerData;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {

    private static ChatManager INSTANCE;

    public final Map<PlayerData, Deque<Component>> MessagesToMimic =
            new ConcurrentHashMap<>();

    private static final int MAX_MESSAGES = 20;
    private static final Random RANDOM = new Random();

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
        if (playerData == null || message == null) {
            return;
        }

        Deque<Component> messages = MessagesToMimic.computeIfAbsent(
                playerData,
                p -> new ArrayDeque<>()
        );

        messages.addLast(message);

        while (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
    }

    public Map<PlayerData, Deque<Component>> getMessagesToMimic() {
        return MessagesToMimic;
    }

    public Component getRandomMessage(PlayerData playerData) {
        if (playerData == null) {
            return null;
        }

        Deque<Component> messages = MessagesToMimic.get(playerData);

        // Player has never sent a message
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        int randomIndex = RANDOM.nextInt(messages.size());

        int index = 0;
        for (Component message : messages) {
            if (index++ == randomIndex) {
                return message;
            }
        }

        return null;
    }
}