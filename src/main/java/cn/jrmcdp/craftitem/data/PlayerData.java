package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.player.IPlayerData;
import cn.jrmcdp.craftitem.data.player.YamlPlayerData;
import cn.jrmcdp.craftitem.func.PlayerDataManager;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PlayerData {
    private final IPlayerData impl;
    public final Player player;

    @Deprecated
    public PlayerData(Player player) {
        this(PlayerDataManager.inst().createData(player), player);
    }

    @Deprecated
    public PlayerData(CraftItem plugin, Player player) {
        this(PlayerDataManager.inst().createData(player), player);
    }

    public PlayerData(IPlayerData impl, Player player) {
        this.player = player;
        this.impl = impl;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Integer getScore(String key) {
        return impl.getScore(key);
    }

    public Integer getFailTimes(String key) {
        return impl.getFailTimes(key);
    }

    public Long getEndTime(String key) {
        return impl.getEndTime(key);
    }

    public Integer addScore(String key, int add) {
        return impl.addScore(key, add);
    }

    public Integer setScore(String key, int score) {
        return impl.setScore(key, score);
    }

    public Integer addFailTimes(String key, int add) {
        return impl.addFailTimes(key, add);
    }

    public void setFailTimes(String key, int score) {
        impl.setFailTimes(key, score);
    }

    public void setTime(String key, long endTime) {
        impl.setTime(key, endTime);
    }

    public void removeTime(String key) {
        impl.removeTime(key);
    }

    public void addTimeForgeCount(String key, int add) {
        impl.addTimeForgeCount(key, add);
    }

    public int getTimeForgeCount(String key) {
        return impl.getTimeForgeCount(key);
    }

    public void addForgeCount(String key, int add) {
        impl.addForgeCount(key, add);
    }

    public int getForgeCount(String key) {
        return impl.getForgeCount(key);
    }

    public void clearScore(String key) {
        impl.clearScore(key);
    }

    public void clearFailTimes(String key) {
        impl.clearFailTimes(key);
    }

    public void save() {
        impl.save();
    }
}
