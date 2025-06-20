package cn.jrmcdp.craftitem.data.player;

import cn.jrmcdp.craftitem.func.PlayerDataManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SQLPlayerData implements IPlayerData {
    private static class HashMap<K, V> extends java.util.HashMap<K, V> {
        private boolean modified;
        public boolean checkModified() {
            if (modified) {
                modified = false;
                return true;
            }
            return false;
        }
        @Override
        public V put(K key, V value) {
            modified = true;
            return super.put(key, value);
        }
        @Override
        public V remove(Object key) {
            modified = true;
            return super.remove(key);
        }
    }
    public final PlayerDataManager database;
    public final Player player;

    /**
     * 各配方的进度，用于 普通锻造 困难锻造
     */
    private final HashMap<String, Integer> normalScoreMap = new HashMap<>();
    /**
     * 各配方的普通/困难锻造进行过的次数
     */
    private final HashMap<String, Integer> normalCountMap = new HashMap<>();
    /**
     * 各配方的锻造失败次数，用于保底
     */
    private final HashMap<String, Integer> normalFailMap = new HashMap<>();
    /**
     * 各配方的时长锻造结束时间 (毫秒级时间戳)
     */
    private final HashMap<String, Long> timeEndMap = new HashMap<>();
    /**
     * 各配方的时长锻造进行过的次数
     */
    private final HashMap<String, Integer> timeCountMap = new HashMap<>();

    public SQLPlayerData(PlayerDataManager database, Player player) {
        this.database = database;
        this.player = player;
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

    public void reload() {

    }

    @Override
    public void save() {
        if (normalScoreMap.checkModified()) {

        }
        if (normalCountMap.checkModified()) {

        }
        if (normalFailMap.checkModified()) {

        }
        if (timeEndMap.checkModified()) {

        }
        if (timeCountMap.checkModified()) {

        }
    }
}
