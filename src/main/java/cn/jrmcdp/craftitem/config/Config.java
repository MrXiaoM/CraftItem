package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import com.cryptomorin.xseries.messages.Titles;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;

import static cn.jrmcdp.craftitem.Utils.valueOf;

public class Config {
    private static FileConfiguration config;

    private static ConfigurationSection setting;
    private static HashMap<String, List<String>> category;
    private static Titles forgeTitle;
    private static Sound soundClickInventory;
    private static Sound soundForgeSuccess;
    private static Sound soundForgeFail;
    private static Sound soundForgeTitle;
    public static void reload() {
        CraftItem.getPlugin().reloadConfig();
        config = CraftItem.getPlugin().getConfig();
        setting = config.getConfigurationSection("Setting");
        if (setting == null) return;

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
        forgeTitle = new Titles(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle), fadeIn, time, fadeOut);

        soundClickInventory = valueOf(Sound.class, setting.getString("Sounds.ClickInventory")).orElse(Sound.UI_BUTTON_CLICK);
        soundForgeSuccess = valueOf(Sound.class, setting.getString("Sounds.ForgeSuccess")).orElse(Sound.BLOCK_ANVIL_USE);
        soundForgeFail = valueOf(Sound.class, setting.getString("Sounds.ForgeFail")).orElse(Sound.BLOCK_GLASS_BREAK);
        soundForgeTitle = valueOf(Sound.class, setting.getString("Sounds.ForgeTitle")).orElse(Sound.BLOCK_ANVIL_LAND);
    }

    public static FileConfiguration getConfig() {
        return config;
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
    public static Titles getForgeTitle() {
        return forgeTitle;
    }

    public static HashMap<String, List<String>> getCategory() {
        return category;
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
