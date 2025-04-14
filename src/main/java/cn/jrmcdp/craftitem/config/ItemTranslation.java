package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.func.AbstractModule;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ItemTranslation extends AbstractModule {
    private static final Map<String, String> material = new HashMap<>();
    public ItemTranslation(CraftItem plugin) {
        super(plugin);
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        YamlConfiguration config = ConfigUtils.loadPluginConfig(plugin, "Material.yml");
        material.clear();
        for (String key : config.getKeys(false)) {
            material.put(key, config.getString(key, "读取异常"));
        }
    }

    public static Map<String, String> getMaterial() {
        return material;
    }

    public static String get(String name, String def) {
        return getMaterial().getOrDefault(name, def);
    }

    public static String get(String name) {
        return get(name, name);
    }
}
