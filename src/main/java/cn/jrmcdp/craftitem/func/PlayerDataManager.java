package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.PlayerData;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@AutoRegister
public class PlayerDataManager extends AbstractModule implements Listener, IDatabase {
    public final Map<String, PlayerData> playerDataHashMap = new HashMap<>();
    boolean useYaml;
    public PlayerDataManager(CraftItem plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        useYaml = plugin.options.database().getDriver() == null;
    }

    @Override
    public void reload(Connection conn, String prefix) throws SQLException {
        // TODO: 创建表
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerDataHashMap.remove(event.getPlayer().getName());
    }

    public PlayerData getOrCreatePlayerData(Player player) {
        PlayerData playerData = playerDataHashMap.get(player.getName());
        if (playerData == null) {
            playerData = new PlayerData(plugin, player);
            playerDataHashMap.put(player.getName(), playerData);
        }
        return playerData;
    }

    public static PlayerDataManager inst() {
        return instanceOf(PlayerDataManager.class);
    }
}
