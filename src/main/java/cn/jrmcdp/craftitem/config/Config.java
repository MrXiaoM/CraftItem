package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import com.cryptomorin.xseries.messages.Titles;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;

public class Config {
    private static FileConfiguration config;

    private static ConfigurationSection setting;
    private static HashMap<String, List<String>> category;
    private static Titles forgeTitle;
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
    }

    public static FileConfiguration getConfig() {
        return config;
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
