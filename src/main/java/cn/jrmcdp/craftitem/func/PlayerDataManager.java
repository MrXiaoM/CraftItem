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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@AutoRegister
public class PlayerDataManager extends AbstractModule implements Listener, IDatabase {
    private final Map<String, PlayerData> playerDataHashMap = new HashMap<>();
    public String TABLE_PLAYERS_NORMAL, TABLE_PLAYERS_TIME;
    private boolean useYaml;
    public PlayerDataManager(CraftItem plugin) {
        super(plugin);
        plugin.options.registerDatabase(this);
        registerEvents();
    }

    public Connection getConnection() {
        return plugin.getConnection();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        useYaml = plugin.options.database().getDriver() == null;
    }

    @Override
    public void reload(Connection conn, String prefix) throws SQLException {
        TABLE_PLAYERS_NORMAL = prefix + "players_normal";
        TABLE_PLAYERS_TIME = prefix + "players_time";

        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + TABLE_PLAYERS_NORMAL + "`(" +
                    "`uuid` VARCHAR(48)," +
                    "`name` VARCHAR(48)," +
                    "`craft` VARCHAR(64)," +
                    "`score` INT," +
                    "`count` INT," +
                    "`fail` INT," +
                    "PRIMARY KEY (`uuid`, `craft`)" +
                ");"
        )) { ps.execute(); }
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + TABLE_PLAYERS_TIME + "`(" +
                    "`uuid` VARCHAR(48)," +
                    "`name` VARCHAR(48)," +
                    "`craft` VARCHAR(64)," +
                    "`end_time` BIGINT," +
                    "`count` INT," +
                    "PRIMARY KEY (`uuid`, `craft`)" +
                ");"
        )) { ps.execute(); }
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
