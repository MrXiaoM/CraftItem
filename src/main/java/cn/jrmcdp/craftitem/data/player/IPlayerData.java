package cn.jrmcdp.craftitem.data.player;

import org.jetbrains.annotations.Nullable;

public interface IPlayerData {
    int getScore(String key);
    int getFailTimes(String key);
    @Nullable
    Long getEndTime(String key);
    int addScore(String key, int add);
    int setScore(String key, int score);
    int addFailTimes(String key, int add);
    void setFailTimes(String key, int score);
    void setTime(String key, long endTime);
    void removeTime(String key);
    void addTimeForgeCount(String key, int add);
    int getTimeForgeCount(String key);
    void addForgeCount(String key, int add);
    int getForgeCount(String key);
    void clearScore(String key);
    void clearFailTimes(String key);
    void save();
}
