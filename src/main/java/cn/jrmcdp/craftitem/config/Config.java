package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Condition;
import cn.jrmcdp.craftitem.config.data.Title;
import com.google.common.collect.Lists;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

import static cn.jrmcdp.craftitem.utils.Utils.valueOf;

public class Config {
    private static ConfigurationSection setting;
    private static HashMap<String, List<String>> category;
    private static Title forgeTitle;
    private static Sound soundClickInventory;
    private static Sound soundForgeSuccess;
    private static Sound soundForgeFail;
    private static Sound soundForgeTitle;
    private static List<String> randomGames;
    private static final List<Condition> timeForgeConditions = new ArrayList<>();
    private static final Map<String, Map<String, Integer>> timeForgeCountLimitGroups = new HashMap<>();
    private static String timeFormatHours, timeFormatHour, timeFormatMinutes, timeFormatMinute, timeFormatSeconds, timeFormatSecond;
    public static void reload() {
        CraftItem.getPlugin().reloadConfig();
        FileConfiguration config = CraftItem.getPlugin().getConfig();
        config.setDefaults(new MemoryConfiguration());
        setting = config.getConfigurationSection("Setting");
        if (setting != null) {
            category = new HashMap<>();
            {
                ConfigurationSection section = setting.getConfigurationSection("Category");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        category.put(key, section.getStringList(key));
                    }
                }
            }
            String title = setting.getString("ForgeTitle.Title", "&a敲敲打打");
            String subtitle = setting.getString("ForgeTitle.SubTitle", "&e锻造中...");
            int fadeIn = setting.getInt("ForgeTitle.FadeIn", 10);
            int time = setting.getInt("ForgeTitle.Time", 20);
            int fadeOut = setting.getInt("ForgeTitle.FadeOut", 10);
            forgeTitle = new Title(title, subtitle, fadeIn, time, fadeOut);

            soundClickInventory = valueOf(Sound.class, setting.getString("Sounds.ClickInventory")).orElse(Sound.UI_BUTTON_CLICK);
            soundForgeSuccess = valueOf(Sound.class, setting.getString("Sounds.ForgeSuccess")).orElse(Sound.BLOCK_ANVIL_USE);
            soundForgeFail = valueOf(Sound.class, setting.getString("Sounds.ForgeFail")).orElse(Sound.BLOCK_GLASS_BREAK);
            soundForgeTitle = valueOf(Sound.class, setting.getString("Sounds.ForgeTitle")).orElse(Sound.BLOCK_ANVIL_LAND);
        }
        randomGames = config.getStringList("RandomGames");
        timeForgeConditions.clear();
        ConfigurationSection section = config.getConfigurationSection("TimeForgeConditions");
        if (section != null) for (String key : section.getKeys(false)) {
            String input = section.getString(key + ".input");
            String type = section.getString(key + ".type");
            String output = section.getString(key + ".output");
            timeForgeConditions.add(new Condition(input, type, output));
        }

        timeFormatHour = config.getString("TimeFormat.Hour", "时");
        timeFormatHours = config.getString("TimeFormat.Hours", "时");
        timeFormatMinute = config.getString("TimeFormat.Minute", "分");
        timeFormatMinutes = config.getString("TimeFormat.Minutes", "分");
        timeFormatSecond = config.getString("TimeFormat.Second", "秒");
        timeFormatSeconds = config.getString("TimeFormat.Seconds", "秒");

        timeForgeCountLimitGroups.clear();
        section = config.getConfigurationSection("TimeForgeCountLimitGroups");
        if (section != null) for (String key : section.getKeys(false)) {
            ConfigurationSection sec = section.getConfigurationSection(key);
            Map<String, Integer> map = new HashMap<>();
            if (sec != null) for (String perm : sec.getKeys(false)) {
                int count = sec.getInt(perm);
                map.put(perm, Math.max(count, 0));
            }
            if (!map.isEmpty()) {
                timeForgeCountLimitGroups.put(key, map);
            }
        }
    }

    public static Sound getSoundClickInventory() {
        return soundClickInventory;
    }
    public static Sound getSoundForgeSuccess() {
        return soundForgeSuccess;
    }
    public static Sound getSoundForgeFail() {
        return soundForgeFail;
    }
    public static Sound getSoundForgeTitle() {
        return soundForgeTitle;
    }
    public static Title getForgeTitle() {
        return forgeTitle;
    }

    public static HashMap<String, List<String>> getCategory() {
        return category;
    }

    public static List<String> getRandomGames() {
        return randomGames;
    }

    public static String getRandomGame() {
        return getRandomGames().get(new Random().nextInt(getRandomGames().size()));
    }

    public static boolean isMeetTimeForgeCondition(Player player) {
        if (player == null) return false;
        for (Condition condition : timeForgeConditions) {
            if (!condition.check(player)) return false;
        }
        return true;
    }

    public static String formatTime(int hour, int minute, int second, String noneTips) {
        if (hour <= 0 && minute <= 0 && second <= 0) return noneTips;
        StringBuilder sb = new StringBuilder();
        String hourUnit = hour > 1 ? timeFormatHours : timeFormatHour;
        String minuteUnit = minute > 1 ? timeFormatMinutes : timeFormatMinute;
        String secondUnit = second > 1 ? timeFormatSeconds : timeFormatSecond;
        if (hour > 0) sb.append(hour).append(hourUnit);
        if (minute > 0) sb.append(minute).append(minuteUnit);
        if (second >= 0) sb.append(second).append(secondUnit);
        return sb.toString();
    }

    public static Map<String, Map<String, Integer>> getTimeForgeCountLimitGroups() {
        return timeForgeCountLimitGroups;
    }

    /**
     * 获取玩家在某个限制组中的限制数量
     * @param player 玩家
     * @param group 组名
     * @return 0 为无限制，-1 为匹配失败
     */
    public static int getTimeForgeCountLimit(Player player, String group) {
        if (group.isEmpty()) return 0;
        Map<String, Integer> map = timeForgeCountLimitGroups.get(group);
        if (map == null || map.isEmpty()) return 0;
        List<Map.Entry<String, Integer>> list = Lists.newArrayList(map.entrySet());
        list.sort(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getValue)));
        int limit = -1;
        for (Map.Entry<String, Integer> entry : list) {
            if (entry.getValue() == 0 && player.hasPermission("craftitem.time." + entry.getKey())) {
                limit = 0;
                break;
            }
            if (entry.getValue() > limit && player.hasPermission("craftitem.time." + entry.getKey())) {
                limit = entry.getValue();
            }
        }
        return limit;
    }

    public static String getChanceName(int chance) {
        ConfigurationSection section = setting.getConfigurationSection("ChanceName");
        if (section != null) for (String key : section.getKeys(false)) {
            int i = Integer.parseInt(key);
            if (chance <= i)
                return section.getString(key);
        }
        return "§c§n未知领域";
    }
}
