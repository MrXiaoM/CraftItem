package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.config.FileConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
        this.failMap = new HashMap<>();
        load("ForgeData", (section, key) -> {
            this.scoreMap.put(key, section.getInt(key));
        });
        load("TimeForgeData", (section, key) -> {
            this.timeMap.put(key, section.getLong(key));
        });
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

    public void removeTime(String key) {
        this.timeMap.remove(key);
    }

    public void clearScore(String key) {
        this.scoreMap.put(key, 0);
    }

    private void load(String key, BiConsumer<ConfigurationSection, String> consumer) {
        ConfigurationSection section = this.config.getConfigurationSection(key);
        if (section != null) {
            for (String k : section.getKeys(false)) {
                consumer.accept(section, k);
            }
        }
    }

    private <T> void save(String section, Map<String, T> map) {
        this.config.set(section, null);
        map.forEach((key, value) -> this.config.set(section + "." + key, value));
    }

    public void save() {
        save("ForgeData", this.scoreMap);
        save("TimeForgeData", this.timeMap);
        FileConfig.Custom.saveConfig("PlayerData", this.player.getName(), this.config);
    }
}
