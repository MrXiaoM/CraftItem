package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.config.FileConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerData {
    private final YamlConfiguration config;

    public final Player player;

    /**
     * 各配方的进度，用于 普通锻造 困难锻造
     */
    public final Map<String, Integer> scoreMap = new HashMap<>();
    /**
     * 各配方的时长锻造结束时间 (毫秒级时间戳)
     */
    public final Map<String, Long> timeMap = new HashMap<>();
    /**
     * 各配方的时长锻造进行过的次数
     */
    public final Map<String, Integer> timeCountMap = new HashMap<>();
    /**
     * 各配方的锻造失败次数，用于保底
     */
    public final Map<String, Integer> failMap = new HashMap<>();

    public PlayerData(Player player) {
        this.player = player;
        this.config = FileConfig.Custom.loadConfig("PlayerData", player.getName());
        load("ForgeData", (section, key) -> this.scoreMap.put(key, section.getInt(key)));
        load("TimeForgeData", (section, key) -> this.timeMap.put(key, section.getLong(key)));
        load("TimeForgeCountData", (section, key) -> this.timeCountMap.put(key, section.getInt(key)));
        load("FailForgeData", (section, key) -> this.failMap.put(key, section.getInt(key)));
    }

    public Player getPlayer() {
        return this.player;
    }

    public Integer getScore(String key) {
        return this.scoreMap.getOrDefault(key, 0);
    }

    public Integer getFailTimes(String key) {
        return this.failMap.getOrDefault(key, 0);
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

    public Integer addFailTimes(String key, int add) {
        Integer score = getFailTimes(key) + add;
        this.failMap.put(key, score);
        return score;
    }

    public void setFailTimes(String key, int score) {
        this.failMap.put(key, score);
    }

    public void setTime(String key, long endTime) {
        endTime = Math.max(System.currentTimeMillis(), endTime);
        this.timeMap.put(key, endTime);
    }

    public void removeTime(String key) {
        this.timeMap.remove(key);
    }

    public void addTimeForgeCount(String key, int add) {
        int count = getTimeForgeCount(key) + add;
        this.timeCountMap.put(key, count);
    }

    public int getTimeForgeCount(String key) {
        return this.timeCountMap.getOrDefault(key, 0);
    }

    public void clearScore(String key) {
        setScore(key, 0);
    }

    public void clearFailTimes(String key) {
        setFailTimes(key, 0);
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
        save("TimeForgeCountData", this.timeCountMap);
        save("FailForgeData", this.failMap);
        FileConfig.Custom.saveConfig("PlayerData", this.player.getName(), this.config);
    }
}
