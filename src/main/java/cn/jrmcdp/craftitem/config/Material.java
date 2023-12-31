package cn.jrmcdp.craftitem.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class Material {
    private static YamlConfiguration config;

    private static HashMap<String, String> material;

    public static void reload() {
        config = FileConfig.Material.getConfig();
        material = new HashMap<>();
        for (String key : config.getKeys(false)) {
            material.put(key, config.getString(key, "读取异常"));
        }
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static HashMap<String, String> getMaterial() {
        return material;
    }
}
