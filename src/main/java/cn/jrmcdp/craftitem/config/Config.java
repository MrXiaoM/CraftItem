package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Condition;
import cn.jrmcdp.craftitem.config.data.Title;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
        ConfigurationSection tfcSection = config.getConfigurationSection("TimeForgeConditions");
        if (tfcSection != null) for (String key : tfcSection.getKeys(false)) {
            String input = tfcSection.getString(key + ".input");
            String type = tfcSection.getString(key + ".type");
            String output = tfcSection.getString(key + ".output");
            timeForgeConditions.add(new Condition(input, type, output));
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
