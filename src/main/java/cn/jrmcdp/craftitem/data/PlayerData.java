package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.config.FileConfig;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerData {
    private final YamlConfiguration config;

    private final Player player;

    private final HashMap<String, Integer> scoreMap;
    private final HashMap<String, Long> timeMap;

    public PlayerData(Player player) {
        this.player = player;
        this.config = FileConfig.Custom.getConfig("PlayerData", player.getName());
        this.scoreMap = new HashMap<>();
        this.timeMap = new HashMap<>();
        ConfigurationSection section = this.config.getConfigurationSection("ForgeData");
        if (section != null)
            for (String key : section.getKeys(false))
                this.scoreMap.put(key, section.getInt(key));
        section = this.config.getConfigurationSection("TimeForgeData");
        if (section != null)
            for (String key : section.getKeys(false))
                this.timeMap.put(key, section.getLong(key));
    }

    public YamlConfiguration getConfig() {
        return this.config;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HashMap<String, Integer> getScoreMap() {
        return this.scoreMap;
    }

    public HashMap<String, Long> getTimeMap() {
        return this.timeMap;
    }

    public Integer getScore(String key) {
        return this.scoreMap.getOrDefault(key, 0);
    }

    public Long getEndTime(String key) {
        return this.timeMap.getOrDefault(key, null);
    }

    public Integer addScore(String key, int add) {
        Integer score = getScore(key);
        if (score == null)
            score = 0;
        score = Math.min(100, Math.max(0, score + add));
        this.scoreMap.put(key, score);
        return score;
    }

    public Integer setScore(String key, int score) {
        score = Math.min(100, Math.max(0, score));
        this.scoreMap.put(key, score);
        return score;
    }

    public Long setTime(String key, long endTime) {
        endTime = Math.max(System.currentTimeMillis(), endTime);
        this.timeMap.put(key, endTime);
        return endTime;
    }

    public Long removeTime(String key) {
        return this.timeMap.remove(key);
    }

    public void clearScore(String key) {
        this.scoreMap.put(key, 0);
    }

    public void save() {
        this.scoreMap.forEach((key, value) -> this.config.set("ForgeData." + key, value));
        this.timeMap.forEach((key, value) -> this.config.set("TimeForgeData." + key, value));
        FileConfig.Custom.saveConfig("PlayerData", this.player.getName(), this.config);
    }
}
