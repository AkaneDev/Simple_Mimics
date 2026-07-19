package au.akanedev.simplemimics.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utilities for working with player data (skins, etc.)
 */
public class PlayerDataUtils {

    private PlayerDataUtils() {}

    /**
     * Get a player's skin texture URL
     * For 1.20.1, this would be stored in the player's GameProfile
     */
    public static String getPlayerSkinUrl(ServerPlayer player) {
        if (player == null) return null;
        
        try {
            var gameProfile = player.getGameProfile();
            if (gameProfile != null && gameProfile.getProperties() != null) {
                var textures = gameProfile.getProperties().get("textures");
                if (textures != null && !textures.isEmpty()) {
                    return textures.iterator().next().value();
                }
            }
        } catch (Exception e) {
            // Ignore - may fail in some edge cases
        }
        
        return null;
    }

    /**
     * Get player username
     */
    public static String getPlayerName(ServerPlayer player) {
        if (player == null) return "Unknown";
        return player.getGameProfile().getName();
    }

    /**
     * Get player's UUID
     */
    public static UUID getPlayerUUID(Player player) {
        if (player == null) return null;
        return player.getUUID();
    }

    /**
     * Check if a player is valid and online
     */
    public static boolean isPlayerOnline(ServerPlayer player) {
        return player != null && player.isAlive();
    }

    /**
     * Get distance between two entities
     */
    public static double getDistance(net.minecraft.world.entity.Entity a, net.minecraft.world.entity.Entity b) {
        if (a == null || b == null) return Double.MAX_VALUE;
        return a.position().distanceTo(b.position());
    }

    /**
     * Save mimic data to NBT
     */
    public static CompoundTag saveMimicData(String skinUrl, String gamertag, String targetAUUID, String targetBUUID) {
        CompoundTag tag = new CompoundTag();
        
        if (skinUrl != null) {
            tag.putString("skinUrl", skinUrl);
        }
        if (gamertag != null) {
            tag.putString("gamertag", gamertag);
        }
        if (targetAUUID != null) {
            tag.putString("targetA", targetAUUID);
        }
        if (targetBUUID != null) {
            tag.putString("targetB", targetBUUID);
        }
        
        return tag;
    }

    /**
     * Load mimic data from NBT
     */
    public static CompoundTag loadMimicData(CompoundTag tag) {
        return tag;
    }

    /**
     * Get nearby players within a radius
     */
    public static List<ServerPlayer> getNearbyPlayers(ServerPlayer center, double radius) {
        List<ServerPlayer> result = new ArrayList<>();
        
        if (center == null || center.level() == null || center.level().getServer() == null) {
            return result;
        }

        var server = center.level().getServer();
        var players = server.getPlayerList().getPlayers();
        var centerPos = center.position();

        for (ServerPlayer player : players) {
            if (player == center) continue;
            
            double dist = centerPos.distanceTo(player.position());
            if (dist <= radius) {
                result.add(player);
            }
        }

        return result;
    }
}