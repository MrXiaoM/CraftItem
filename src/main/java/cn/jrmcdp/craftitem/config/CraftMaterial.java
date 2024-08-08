package cn.jrmcdp.craftitem.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class CraftMaterial {
    private static final Map<String, String> material = new HashMap<>();

    public static void reload() {
        YamlConfiguration config = FileConfig.Material.loadConfig();
        material.clear();
        for (String key : config.getKeys(false)) {
            material.put(key, config.getString(key, "读取异常"));
        }
    }

    public static Map<String, String> getMaterial() {
        return material;
    }
}
