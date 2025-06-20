package cn.jrmcdp.craftitem.data.player;

import cn.jrmcdp.craftitem.func.PlayerDataManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SQLPlayerData implements IPlayerData {
    private static class MapWithLogs<V> extends java.util.HashMap<String, V> {
        private Set<String> modified = new HashSet<>();
        private Set<String> checkModified() {
            if (modified.isEmpty()) return modified;
            HashSet<String> keys = new HashSet<>(modified);
            modified.clear();
            return keys;
        }
        @Override
        public V put(String key, V value) {
            modified.add(key);
            return super.put(key, value);
        }
        @Override
        public V remove(Object key) {
            modified.add(String.valueOf(key));
            return super.remove(key);
        }
    }
    public final PlayerDataManager database;
    public final Player player;

    /**
     * 各配方的进度，用于 普通锻造 困难锻造
     */
    private final MapWithLogs<Integer> normalScoreMap = new MapWithLogs<>();
    /**
     * 各配方的普通/困难锻造进行过的次数
     */
    private final MapWithLogs<Integer> normalCountMap = new MapWithLogs<>();
    /**
     * 各配方的锻造失败次数，用于保底
     */
    private final MapWithLogs<Integer> normalFailMap = new MapWithLogs<>();
    /**
     * 各配方的时长锻造结束时间 (毫秒级时间戳)
     */
    private final MapWithLogs<Long> timeEndMap = new MapWithLogs<>();
    /**
     * 各配方的时长锻造进行过的次数
     */
    private final MapWithLogs<Integer> timeCountMap = new MapWithLogs<>();

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

    private void clear(Map<?, ?>... maps) {
        for (Map<?, ?> map : maps) {
            map.clear();
        }
    }

    public void reload() {
        clear(normalScoreMap, normalCountMap, normalFailMap, timeEndMap, timeCountMap);
        try (Connection conn = database.getConnection()) {
            String uuid = player.getUniqueId().toString();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM `" + database.TABLE_PLAYERS_NORMAL + "` WHERE `uuid`=?;"
            )) {
                ps.setString(1, uuid);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        String craft = resultSet.getString("craft");
                        int score = resultSet.getInt("score");
                        int count = resultSet.getInt("count");
                        int fail = resultSet.getInt("fail");
                        normalScoreMap.put(craft, score);
                        normalCountMap.put(craft, count);
                        normalFailMap.put(craft, fail);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM `" + database.TABLE_PLAYERS_TIME + "` WHERE `uuid`=?;"
            )) {
                ps.setString(1, uuid);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        String craft = resultSet.getString("craft");
                        long endTime = resultSet.getLong("end_time");
                        int count = resultSet.getInt("count");
                        if (endTime > 114514L) {
                            timeEndMap.put(craft, endTime);
                        }
                        timeCountMap.put(craft, count);
                    }
                }
            }
        } catch (SQLException e) {
            database.warn(e);
        }
    }

    private Set<String> combine(MapWithLogs<?>... maps) {
        Set<String> keys = new HashSet<>();
        for (MapWithLogs<?> map : maps) {
            keys.addAll(map.checkModified());
        }
        return keys;
    }

    @Override
    public void save() {
        Set<String> normalKeys = combine(normalScoreMap, normalCountMap, normalFailMap);
        Set<String> timeKeys = combine(timeEndMap, timeCountMap);
        if (normalKeys.isEmpty() && timeKeys.isEmpty()) return;
        database.plugin.getScheduler().runTaskAsync(() -> save(normalKeys, timeKeys));
    }

    private void save(Set<String> normalKeys, Set<String> timeKeys) {
        try (Connection conn = database.getConnection()) {
            boolean isMySQL = database.plugin.options.database().isMySQL();
            String uuid = player.getUniqueId().toString();
            String name = player.getName();
            if (!normalKeys.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(isMySQL
                        ? "INSERT INTO `" + database.TABLE_PLAYERS_NORMAL + "`(`uuid`,`name`,`craft`,`score`,`count`,`fail`) VALUES(?,?,?,?,?,?) on duplicate key update `name`=?, `score`=?, `count`=?, `fail`=?;"
                        : "INSERT OR REPLACE INTO `" + database.TABLE_PLAYERS_NORMAL + "`(`uuid`,`name`,`craft`,`score`,`count`,`fail`) VALUES(?,?,?,?,?,?);"
                )) {
                    for (String key : normalKeys) {
                        ps.setString(1, uuid);
                        ps.setString(2, name);
                        ps.setString(3, key);
                        ps.setInt(4, normalScoreMap.getOrDefault(key, 0));
                        ps.setInt(5, normalCountMap.getOrDefault(key, 0));
                        ps.setInt(6, normalFailMap.getOrDefault(key, 0));
                        if (isMySQL) {
                            ps.setString(7, name);
                            ps.setInt(8, normalScoreMap.getOrDefault(key, 0));
                            ps.setInt(9, normalCountMap.getOrDefault(key, 0));
                            ps.setInt(10, normalFailMap.getOrDefault(key, 0));
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            if (!timeKeys.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(isMySQL
                        ? "INSERT INTO `" + database.TABLE_PLAYERS_TIME + "`(`uuid`,`name`,`craft`,`end_time`,`count`) VALUES(?,?,?,?,?) on duplicate key update `name`=?, `end_time`=?, `count`=?;"
                        : "INSERT OR REPLACE INTO `" + database.TABLE_PLAYERS_TIME + "`(`uuid`,`name`,`craft`,`end_time`,`count`) VALUES(?,?,?,?,?);"
                )) {
                    for (String key : normalKeys) {
                        ps.setString(1, uuid);
                        ps.setString(2, name);
                        ps.setString(3, key);
                        ps.setLong(4, timeEndMap.getOrDefault(key, 0L));
                        ps.setInt(5, timeCountMap.getOrDefault(key, 0));
                        if (isMySQL) {
                            ps.setString(6, name);
                            ps.setLong(7, timeEndMap.getOrDefault(key, 0L));
                            ps.setInt(8, timeCountMap.getOrDefault(key, 0));
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        } catch (SQLException e) {
            database.warn(e);
        }
    }
}
