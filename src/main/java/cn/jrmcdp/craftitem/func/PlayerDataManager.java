package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.data.player.IPlayerData;
import cn.jrmcdp.craftitem.data.player.SQLPlayerData;
import cn.jrmcdp.craftitem.data.player.YamlPlayerData;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@AutoRegister
public class PlayerDataManager extends AbstractModule implements Listener, IDatabase {
    private final Map<String, PlayerData> playerDataHashMap = new HashMap<>();
    private String TABLE_PLAYERS;
    private boolean useYaml;
    public PlayerDataManager(CraftItem plugin) {
        super(plugin);
        plugin.options.registerDatabase(this);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        useYaml = plugin.options.database().getDriver() == null;
    }

    @Override
    public void reload(Connection conn, String prefix) throws SQLException {
        TABLE_PLAYERS = prefix + "players";
        // TODO: 创建表
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData data = removePlayerDataCache(event.getPlayer());
        if (data != null) {
            data.save();
        }
    }

    @NotNull
    public PlayerData getOrCreatePlayerData(Player player) {
        synchronized (playerDataHashMap) {
            PlayerData playerData = playerDataHashMap.get(player.getName());
            if (playerData == null) {
                IPlayerData data = createData(player);
                playerData = new PlayerData(data, player);
                playerDataHashMap.put(player.getName(), playerData);
            }
            return playerData;
        }
    }

    @Nullable
    public PlayerData removePlayerDataCache(Player player) {
        return removePlayerDataCache(player.getName());
    }

    @Nullable
    public PlayerData removePlayerDataCache(String playerName) {
        return playerDataHashMap.remove(playerName);
    }

    @NotNull
    public IPlayerData createData(Player player) {
        if (useYaml) {
            return new YamlPlayerData(plugin, "./PlayerData", player);
        } else {
            SQLPlayerData data = new SQLPlayerData(this, player);
            data.reload();
            return data;
        }
    }

    public static PlayerDataManager inst() {
        return instanceOf(PlayerDataManager.class);
    }
}
