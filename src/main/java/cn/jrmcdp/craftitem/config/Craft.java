package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.data.CraftData;

import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;

public class Craft {
    private static YamlConfiguration config;

    private static HashMap<String, CraftData> craftDataMap;

    public static void reload() {
        config = FileConfig.Craft.getConfig();
        craftDataMap = new HashMap<>();
        for (String key : config.getKeys(false)) {
            craftDataMap.put(key, (CraftData) config.get(key, CraftData.class));
        }
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static HashMap<String, CraftData> getCraftDataMap() {
        return craftDataMap;
    }

    public static CraftData getCraftData(String key) {
        return craftDataMap.get(key);
    }

    public static void save(String id, CraftData craftData) {
        craftDataMap.put(id, craftData);
        config.set(id, craftData);
        FileConfig.Craft.saveConfig(config);
    }
    public static void delete(String id) {
        craftDataMap.remove(id);
        config.set(id, null);
        FileConfig.Craft.saveConfig(config);
    }

}
