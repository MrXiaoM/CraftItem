package cn.jrmcdp.craftitem.utils;

import java.util.*;

import cn.jrmcdp.craftitem.config.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {
    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static org.bukkit.Material getMaterial(String... ids) {
        return getMaterial(org.bukkit.Material.STONE, ids);
    }

    public static org.bukkit.Material getMaterial(org.bukkit.Material def, String... ids) {
        for (String id : ids) {
            org.bukkit.Material material = org.bukkit.Material.getMaterial(id.toUpperCase());
            if (material != null) return material;
        }
        return def;
    }

    public static List<String> itemToListString(Collection<ItemStack> collection) {
        List<String> list = new ArrayList<>();
        for (ItemStack itemStack : collection) {
            list.add("§a" + Utils.getItemName(itemStack) + "§fx" + itemStack.getAmount());
        }
        return list;
    }

    public static Map<ItemStack, Integer> getAmountMap(List<ItemStack> list) {
        Map<ItemStack, Integer> map = new HashMap<>();
        for (ItemStack itemStack : list) {
            ItemStack item = itemStack.clone();
            item.setAmount(1);
            if (map.containsKey(item)) {
                map.put(item, map.get(item) + itemStack.getAmount());
                continue;
            }
            map.put(item, itemStack.getAmount());
        }
        return map;
    }

    public static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double tryParseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Optional<org.bukkit.Material> parseMaterial(String s) {
        return valueOf(org.bukkit.Material.class, s);
    }

    public static <T extends Enum<?>> Optional<T> valueOf(Class<T> clazz, String s) {
        if (s != null) for (T t : clazz.getEnumConstants()) {
            if (t.name().equalsIgnoreCase(s)) return Optional.of(t);
        }
        return Optional.empty();
    }

    public static String getItemName(ItemStack itemStack) {
        if (itemStack == null) return "空";
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.hasDisplayName()) return itemMeta.getDisplayName();
        }
        String name = itemStack.getType().name();
        return Material.getMaterial().getOrDefault(name, name);
    }
}
