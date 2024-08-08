package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.CraftData;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;

public class Craft {
    private static YamlConfiguration config;

    private static final Map<String, CraftData> craftDataMap = new HashMap<>();

    public static void reload() {
        config = FileConfig.Craft.loadConfig();
        craftDataMap.clear();
        for (String key : config.getKeys(false)) {
            Object object = config.get(key, null);
            if (object instanceof CraftData) {
                craftDataMap.put(key, (CraftData) object);
            } else {
                Logger logger = CraftItem.getPlugin().getLogger();
                logger.warning("无法读取 Craft.yml 的项目 " + key + ": " + object);
            }
        }
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static Map<String, CraftData> getCraftDataMap() {
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
