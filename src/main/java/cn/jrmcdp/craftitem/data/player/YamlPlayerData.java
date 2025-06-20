package cn.jrmcdp.craftitem.data.player;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class YamlPlayerData implements IPlayerData {
    private final CraftItem plugin;
    private final YamlConfiguration config;
    private final File configFile;
    public final Player player;

    /**
     * 各配方的进度，用于 普通锻造 困难锻造
     */
    private final Map<String, Integer> normalScoreMap = new HashMap<>();
    /**
     * 各配方的普通/困难锻造进行过的次数
     */
    private final Map<String, Integer> normalCountMap = new HashMap<>();
    /**
     * 各配方的锻造失败次数，用于保底
     */
    private final Map<String, Integer> normalFailMap = new HashMap<>();
    /**
     * 各配方的时长锻造结束时间 (毫秒级时间戳)
     */
    private final Map<String, Long> timeEndMap = new HashMap<>();
    /**
     * 各配方的时长锻造进行过的次数
     */
    private final Map<String, Integer> timeCountMap = new HashMap<>();

    public YamlPlayerData(CraftItem plugin, String folder, Player player) {
        this.plugin = plugin;
        // 旧版本插件数据兼容 - 使用玩家名储存
        File file = new File(plugin.resolve(folder), player.getName() + ".yml");
        // 旧版文件不存在时，才使用 uuid 储存
        if (!file.exists()) {
            file = new File(plugin.resolve(folder), player.getUniqueId() + ".yml");
        }
        this.configFile = file;
        this.player = player;
        this.config = new YamlConfiguration();
        this.config.options().pathSeparator(' ');
        ConfigUtils.load(config, configFile);
        fromConfig("ForgeData", (section, key) -> this.normalScoreMap.put(key, section.getInt(key)));
        fromConfig("ForgeCountData", (section, key) -> this.normalCountMap.put(key, section.getInt(key)));
        fromConfig("FailForgeData", (section, key) -> this.normalFailMap.put(key, section.getInt(key)));
        fromConfig("TimeForgeData", (section, key) -> this.timeEndMap.put(key, section.getLong(key)));
        fromConfig("TimeForgeCountData", (section, key) -> this.timeCountMap.put(key, section.getInt(key)));
    }

    @Override
    public int getScore(String key) {
        return this.normalScoreMap.getOrDefault(key, 0);
    }

    @Override
    public int getFailTimes(String key) {
        return this.normalFailMap.getOrDefault(key, 0);
    }

    @Nullable
    @Override
    public Long getEndTime(String key) {
        return this.timeEndMap.getOrDefault(key, null);
    }

    @Override
    public int addScore(String key, int add) {
        Integer score = getScore(key);
        if (score == null)
            score = 0;
        score = Math.min(100, Math.max(0, score + add));
        this.normalScoreMap.put(key, score);
        return score;
    }

    @Override
    public int setScore(String key, int score) {
        score = Math.min(100, Math.max(0, score));
        this.normalScoreMap.put(key, score);
        return score;
    }

    @Override
    public int addFailTimes(String key, int add) {
        Integer score = getFailTimes(key) + add;
        this.normalFailMap.put(key, score);
        return score;
    }

    @Override
    public void setFailTimes(String key, int score) {
        this.normalFailMap.put(key, score);
    }

    @Override
    public void setTime(String key, long endTime) {
        endTime = Math.max(System.currentTimeMillis(), endTime);
        this.timeEndMap.put(key, endTime);
    }

    @Override
    public void removeTime(String key) {
        this.timeEndMap.remove(key);
    }

    @Override
    public void addTimeForgeCount(String key, int add) {
        int count = getTimeForgeCount(key) + add;
        this.timeCountMap.put(key, count);
    }

    @Override
    public int getTimeForgeCount(String key) {
        return this.timeCountMap.getOrDefault(key, 0);
    }

    @Override
    public void addForgeCount(String key, int add) {
        int count = getForgeCount(key) + add;
        this.timeCountMap.put(key, count);
    }

    @Override
    public int getForgeCount(String key) {
        return this.timeCountMap.getOrDefault(key, 0);
    }

    @Override
    public void clearScore(String key) {
        setScore(key, 0);
    }

    @Override
    public void clearFailTimes(String key) {
        setFailTimes(key, 0);
    }

    @Override
    public void save() {
        this.config.set("Player.UUID", player.getUniqueId().toString());
        this.config.set("Player.LastKnownName", player.getName());
        toConfig("ForgeData", this.normalScoreMap);
        toConfig("ForgeCountData", this.normalCountMap);
        toConfig("FailForgeData", this.normalFailMap);
        toConfig("TimeForgeData", this.timeEndMap);
        toConfig("TimeForgeCountData", this.timeCountMap);
        ConfigUtils.save(this.config, this.configFile);
    }

    private void fromConfig(String key, BiConsumer<ConfigurationSection, String> consumer) {
        ConfigurationSection section = this.config.getConfigurationSection(key);
        if (section != null) for (String k : section.getKeys(false)) {
            consumer.accept(section, k);
        }
    }

    private <T> void toConfig(String section, Map<String, T> map) {
        this.config.set(section, null);
        map.forEach((key, value) -> this.config.set(section + " " + key, value));
    }
}
