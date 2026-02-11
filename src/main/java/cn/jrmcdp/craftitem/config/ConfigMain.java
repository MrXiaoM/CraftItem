package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Condition;
import cn.jrmcdp.craftitem.config.data.NotDisappear;
import cn.jrmcdp.craftitem.config.data.Sound;
import cn.jrmcdp.craftitem.config.data.Title;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.func.AbstractModule;
import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ConfigUpdater;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.*;

@AutoRegister
public class ConfigMain extends AbstractModule {
    private List<Integer> chanceNamesKeys;
    private Map<Integer, String> chanceNames;
    private String chanceNameUnknown;

    private Map<String, List<String>> category;
    private Title forgeTitle;

    private Sound soundClickInventory;
    private Sound soundForgeSuccess;
    private Sound soundForgeFail;
    private Sound soundForgeTitle;

    private List<String> randomGames;
    private final List<Condition> timeForgeConditions = new ArrayList<>();
    private final Map<String, Map<String, Integer>> countLimitGroups = new HashMap<>();
    private String timeFormatHours, timeFormatHour, timeFormatMinutes, timeFormatMinute, timeFormatSeconds, timeFormatSecond;
    private final NotDisappear notDisappear = new NotDisappear();
    private final Map<String, String> currencyNames = new HashMap<>();
    private final ConfigUpdater updater;

    public ConfigMain(CraftItem plugin) {
        super(plugin);
        updater = ConfigUpdater.create(plugin, "config.yml");
        updater.prefixMatch("Setting.")
                .fullMatch("RandomGames")
                .fullMatch("CurrencyNames.Vault")
                .fullMatch("CurrencyNames.PlayerPoints")
                .fullMatch("CurrencyNames.MPoints")
                .fullMatch("CurrencyNames.NyEconomy")
                .prefixMatch("TimeFormat.")
                .prefixMatch("Material-Adapters.")
                .fullMatch("DoNotDisappear.Material")
                .fullMatch("DoNotDisappear.Name")
                .fullMatch("DoNotDisappear.Lore")
                .prefixMatch("Events.")
                .prefixMatch("offset-characters.");
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        if (plugin.isEnableConfigUpdater() && config instanceof YamlConfiguration) {
            updater.apply((YamlConfiguration) config, plugin.resolve("./config.yml"));
        }
        ConfigurationSection setting = config.getConfigurationSection("Setting");
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
            chanceNamesKeys = new ArrayList<>();
            chanceNames = new HashMap<>();
            {
                chanceNameUnknown = setting.getString("ChanceNameUnknown", "<red><u>未知领域</u>");
                ConfigurationSection section = setting.getConfigurationSection("ChanceName");
                if (section != null) for (String key : section.getKeys(false)) {
                    Integer i = Util.parseInt(key).orElse(null);
                    if (i == null) continue;
                    String name = section.getString(key);
                    chanceNamesKeys.add(i);
                    chanceNames.put(i, name);
                }
                chanceNamesKeys.sort(Comparator.comparingInt(it -> it));
            }
            String title = setting.getString("ForgeTitle.Title", "<green>敲敲打打");
            String subtitle = setting.getString("ForgeTitle.SubTitle", "<yellow>锻造中...");
            int fadeIn = setting.getInt("ForgeTitle.FadeIn", 10);
            int time = setting.getInt("ForgeTitle.Time", 20);
            int fadeOut = setting.getInt("ForgeTitle.FadeOut", 10);
            forgeTitle = new Title(title, subtitle, fadeIn, time, fadeOut);

            soundClickInventory = new Sound(setting, "Sounds.ClickInventory", "UI_BUTTON_CLICK", "CLICK").setPitch(2.0f);
            soundForgeSuccess = new Sound(setting, "Sounds.ForgeSuccess", "BLOCK_ANVIL_USE", "ANVIL_USE");
            soundForgeFail = new Sound(setting, "Sounds.ForgeFail", "BLOCK_GLASS_BREAK", "GLASS");
            soundForgeTitle = new Sound(setting, "Sounds.ForgeTitle", "BLOCK_ANVIL_LAND", "ANVIL_LAND").setPitch(0.8f);
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

        countLimitGroups.clear();
        section = config.getConfigurationSection("CountLimitGroups");
        if (section != null) for (String key : section.getKeys(false)) {
            ConfigurationSection sec = section.getConfigurationSection(key);
            Map<String, Integer> map = new HashMap<>();
            if (sec != null) for (String perm : sec.getKeys(false)) {
                int count = sec.getInt(perm);
                map.put(perm, Math.max(count, 0));
            }
            if (!map.isEmpty()) {
                countLimitGroups.put(key, map);
            }
        }

        notDisappear.reloadConfig(config);

        currencyNames.clear();
        section = config.getConfigurationSection("CurrencyNames");
        if (section != null) for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                ConfigurationSection section1 = section.getConfigurationSection(key);
                if (section1 == null) continue;
                for (String key1 : section1.getKeys(false)) {
                    String value = section1.getString(key1);
                    currencyNames.put(key.toLowerCase() + ":" + key1.toLowerCase(), value);
                }
            }
            String value = section.getString(key);
            currencyNames.put(key.toLowerCase(), value);
        }
    }

    public NotDisappear getNotDisappear() {
        return notDisappear;
    }

    public String getCurrencyName(String pluginName) {
        return currencyNames.getOrDefault(pluginName.toLowerCase(), pluginName);
    }

    public String getCurrencyName(String pluginName, String currencyId) {
        return currencyNames.getOrDefault(pluginName.toLowerCase() + ":" + currencyId.toLowerCase(), currencyId);
    }

    public List<MaterialInstance> filterMaterials(List<MaterialInstance> materials) {
        return notDisappear.filterMaterials(materials);
    }

    @Deprecated
    public boolean isNotDisappearItem(ItemStack item) {
        return notDisappear.isNotDisappearItem(item);
    }

    public Sound getSoundClickInventory() {
        return soundClickInventory;
    }

    public Sound getSoundForgeSuccess() {
        return soundForgeSuccess;
    }

    public Sound getSoundForgeFail() {
        return soundForgeFail;
    }

    public Sound getSoundForgeTitle() {
        return soundForgeTitle;
    }

    public void sendForgeTitle(Player player) {
        forgeTitle.send(player);
    }

    public Map<String, List<String>> getCategory() {
        return category;
    }

    public List<String> getRandomGames() {
        return randomGames;
    }

    @Nullable
    public String getRandomGame() {
        List<String> list = getRandomGames();
        int size = list.size();
        if (size == 0) return null;
        if (size == 1) return list.get(0);
        return list.get(new Random().nextInt(size));
    }

    public boolean isMeetTimeForgeCondition(Player player) {
        if (player == null) return false;
        for (Condition condition : timeForgeConditions) {
            if (!condition.check(player)) return false;
        }
        return true;
    }

    public String formatTime(int hour, int minute, int second, String noneTips) {
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

    public String getTimeDisplay(long second, String noneTips) {
        int hour = 0, minute = 0;
        while (second >= 3600) {
            second -= 3600;
            hour++;
        }
        while (second >= 60) {
            second -= 60;
            minute++;
        }
        return formatTime(hour, minute, (int) second, noneTips);
    }

    public Map<String, Map<String, Integer>> getCountLimitGroups() {
        return countLimitGroups;
    }

    /**
     * 获取玩家在某个限制组中的限制数量
     * @param player 玩家
     * @param group 组名
     * @return 0 为无限制，-1 为匹配失败
     */
    public int getCountLimit(Player player, String group) {
        if (group.isEmpty()) return 0;
        Map<String, Integer> map = countLimitGroups.get(group);
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

    public String getChanceName(int chance) {
        for (Integer i : chanceNamesKeys) {
            if (chance <= i)
                return chanceNames.get(i);
        }
        return chanceNameUnknown;
    }

    public static ConfigMain inst() {
        return instanceOf(ConfigMain.class);
    }
}
