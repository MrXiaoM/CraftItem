package cn.jrmcdp.craftitem.manager;

import cn.jrmcdp.craftitem.data.PlayerData;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class DataManager {
    public static final Map<String, PlayerData> playerDataHashMap = new HashMap<>();

    public static PlayerData getOrCreatePlayerData(Player player) {
        PlayerData playerData = playerDataHashMap.get(player.getName());
        if (playerData == null) {
            playerData = new PlayerData(player);
            playerDataHashMap.put(player.getName(), playerData);
        }
        return playerData;
    }
}
