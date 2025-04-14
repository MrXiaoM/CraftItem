package cn.jrmcdp.craftitem.manager;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.func.AbstractModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.HashMap;
import java.util.Map;

@AutoRegister
public class PlayerDataManager extends AbstractModule implements Listener {
    public final Map<String, PlayerData> playerDataHashMap = new HashMap<>();
    public PlayerDataManager(CraftItem plugin) {
        super(plugin);
        registerEvents();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerDataHashMap.remove(event.getPlayer().getName());
    }

    public PlayerData getOrCreatePlayerData(Player player) {
        PlayerData playerData = playerDataHashMap.get(player.getName());
        if (playerData == null) {
            playerData = new PlayerData(player);
            playerDataHashMap.put(player.getName(), playerData);
        }
        return playerData;
    }

    public static PlayerDataManager inst() {
        return instanceOf(PlayerDataManager.class);
    }
}
