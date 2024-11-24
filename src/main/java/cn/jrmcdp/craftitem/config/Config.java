package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Condition;
import cn.jrmcdp.craftitem.config.data.Title;
import cn.jrmcdp.craftitem.utils.Utils;
import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static cn.jrmcdp.craftitem.utils.Utils.valueOf;

public class Config {
    private static ConfigurationSection setting;
    private static HashMap<String, List<String>> category;
    private static Title forgeTitle;
    private static @Nullable Sound soundClickInventory;
    private static @Nullable Sound soundForgeSuccess;
    private static @Nullable Sound soundForgeFail;
    private static @Nullable Sound soundForgeTitle;
    private static List<String> randomGames;
    private static final List<Condition> timeForgeConditions = new ArrayList<>();
    private static final Map<String, Map<String, Integer>> countLimitGroups = new HashMap<>();
    private static String timeFormatHours, timeFormatHour, timeFormatMinutes, timeFormatMinute, timeFormatSeconds, timeFormatSecond;
    private static final List<Material> notDisappearMaterials = new ArrayList<>();
    private static final List<String> notDisappearNames = new ArrayList<>();
    private static final List<String> notDisappearLores = new ArrayList<>();
    private static final Map<String, List<String>> notDisappearNBTStrings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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

            soundClickInventory = valueOf(Sound.class, setting.getString("Sounds.ClickInventory"))
                    .orElseGet(() -> valueOf(Sound.class, "UI_BUTTON_CLICK")
                            .orElseGet(() -> valueOf(Sound.class, "CLICK").orElse(null)));
            soundForgeSuccess = valueOf(Sound.class, setting.getString("Sounds.ForgeSuccess"))
                    .orElseGet(() -> valueOf(Sound.class, "BLOCK_ANVIL_USE")
                            .orElseGet(() -> valueOf(Sound.class, "ANVIL_USE").orElse(null)));
            soundForgeFail = valueOf(Sound.class, setting.getString("Sounds.ForgeFail"))
                    .orElseGet(() -> valueOf(Sound.class, "BLOCK_GLASS_BREAK")
                            .orElseGet(() -> valueOf(Sound.class, "GLASS").orElse(null)));
            soundForgeTitle = valueOf(Sound.class, setting.getString("Sounds.ForgeTitle"))
                    .orElseGet(() -> valueOf(Sound.class, "BLOCK_ANVIL_LAND")
                            .orElseGet(() -> valueOf(Sound.class, "ANVIL_LAND").orElse(null)));
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

        notDisappearMaterials.clear();
        notDisappearNames.clear();
        notDisappearLores.clear();
        notDisappearNBTStrings.clear();
        for (String s : config.getStringList("DoNotDisappear.Material")) {
            Material material = Utils.parseMaterial(s).orElse(null);
            if (material == null) continue;
            notDisappearMaterials.add(material);
        }
        for (String s : config.getStringList("DoNotDisappear.Name")) {
            notDisappearNames.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        for (String s : config.getStringList("DoNotDisappear.Lore")) {
            notDisappearLores.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        section = config.getConfigurationSection("DoNotDisappear.NBTString");
        if (section != null) for (String key : section.getKeys(false)) {
            List<String> list = section.getStringList(key);
            notDisappearNBTStrings.put(key, list);
        }
    }

    public static List<ItemStack> filterMaterials(List<ItemStack> materials) {
        List<ItemStack> list = new ArrayList<>();
        if (materials.isEmpty()) return list;
        for (ItemStack material : materials) {
            if (isNotDisappearItem(material)) continue;
            list.add(material);
        }
        return list;
    }

    public static boolean isNotDisappearItem(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return true;
        if (notDisappearMaterials.contains(item.getType())) return true;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
            if (displayName != null && !displayName.isEmpty()) {
                for (String s : notDisappearNames) {
                    if (displayName.contains(s)) return true;
                }
            }
            List<String> lore = meta.hasLore() ? meta.getLore() : null;
            if (lore != null && !lore.isEmpty()) {
                String loreStr = String.join("\n", lore);
                for (String s : notDisappearLores) {
                    if (loreStr.contains(s)) return true;
                }
            }
        }
        if (!notDisappearNBTStrings.isEmpty()) {
            return NBT.get(item, nbt -> {
                for (Map.Entry<String, List<String>> entry : notDisappearNBTStrings.entrySet()) {
                    if (nbt.hasTag(entry.getKey(), NBTType.NBTTagString)) {
                        List<String> list = entry.getValue();
                        if (list.isEmpty()) return true;
                        String value = nbt.getString(entry.getKey());
                        for (String s : list) {
                            if (value.contains(s)) return true;
                        }
                    }
                }
                return false;
            });
        }
        return false;
    }

    public static void playSoundClickInventory(Player player) {
        if (soundClickInventory == null) return;
        player.playSound(player.getLocation(), soundClickInventory, 1.0f, 2.0f);
    }
    public static void playSoundForgeSuccess(Player player) {
        if (soundForgeSuccess == null) return;
        player.playSound(player.getLocation(), soundForgeSuccess, 1.0f, 1.0f);
    }
    public static void playSoundForgeFail(Player player) {
        if (soundForgeFail == null) return;
        player.playSound(player.getLocation(), soundForgeFail, 1.0f, 1.0f);
    }
    public static void playSoundForgeTitle(Player player) {
        if (soundForgeTitle == null) return;
        player.playSound(player.getLocation(), soundForgeTitle, 1.0f, 0.8f);
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

    public static Map<String, Map<String, Integer>> getCountLimitGroups() {
        return countLimitGroups;
    }

    /**
     * 获取玩家在某个限制组中的限制数量
     * @param player 玩家
     * @param group 组名
     * @return 0 为无限制，-1 为匹配失败
     */
    public static int getCountLimit(Player player, String group) {
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
