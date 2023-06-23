package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;

public class Config {
    private static FileConfiguration config;

    private static ConfigurationSection setting;
    private static HashMap<String, List<String>> category;

    public static void reload() {
        CraftItem.getPlugin().reloadConfig();
        config = CraftItem.getPlugin().getConfig();
        setting = config.getConfigurationSection("Setting");

        category = new HashMap<>();
        {
            ConfigurationSection section = setting.getConfigurationSection("Category");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    category.put(key, section.getStringList(key));
                }
            }
        }
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static ConfigurationSection getSetting() {
        return setting;
    }

    public static HashMap<String, List<String>> getCategory() {
        return category;
    }

    public static String getChanceName(int chance) {
        ConfigurationSection section = setting.getConfigurationSection("ChanceName");
        for (String key : section.getKeys(false)) {
            int i = Integer.parseInt(key);
            if (chance <= i)
                return section.getString(key);
        }
        return "§c§n未知领域";
    }
}
