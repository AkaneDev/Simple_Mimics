package au.akanedev.simplemimics.util;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class PlayerData {
    final UUID uuid;
    final Player player;

    public PlayerData(UUID uuid, Player player) {
        this.uuid = uuid;
        this.player = player;
    }
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.player = PlayerDataUtils.getPlayerFromUUID(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayerData other)) return false;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}