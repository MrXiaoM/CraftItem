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

    public static String getItemName(ItemStack itemStack) {
        if (itemStack == null) return "空";
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.hasDisplayName()) return itemMeta.getDisplayName();
        }
        String name = itemStack.getType().name();
        return material.containsKey(name) ? material.get(name) : name;
    }
}
