package cn.jrmcdp.craftitem.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import cn.jrmcdp.craftitem.config.CraftMaterial;
import cn.jrmcdp.craftitem.minigames.utils.LogUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

    @CanIgnoreReturnValue
    public static boolean createDirectory(File file) {
        return !file.exists() && file.mkdirs();
    }

    @CanIgnoreReturnValue
    public static boolean createNewFile(File file) {
        if (file.exists()) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            LogUtils.warn("创建文件 " + file.getName() + " 时出现一个错误", e);
            return false;
        }
    }

    public static void updateInventory(Player player) {
        player.updateInventory();
    }

    public static ItemStack getItemStack(Material material, String name) {
        return getItemStack(material, name, new ArrayList<>());
    }
    public static ItemStack getItemStack(Material material, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static Material getMaterial(String... ids) {
        return getMaterial(Material.STONE, ids);
    }

    public static Material getMaterial(Material def, String... ids) {
        for (String id : ids) {
            Material material = Material.getMaterial(id.toUpperCase());
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

    public static Optional<Material> parseMaterial(String s) {
        Class<Material> m = Material.class;
        Optional<Material> material = valueOf(m, s);
        if (!material.isPresent()) { // some legacy material (1.12.2 and lower)
            String lower = s.toLowerCase();
            if (lower.contains("stained_glass_pane")) return valueOf(m, "stained_glass_pane");
            if (lower.contains("stained_glass")) return valueOf(m, "stained_glass");
            if (lower.contains("terracotta")) return valueOf(m, "stained_clay");
            if (lower.contains("banner") && !lower.contains("pattern")) return valueOf(m, "stained_banner");
            if (lower.equals("clock")) return valueOf(m, "watch");
            if (lower.contains("bed")) return valueOf(m, "bed");
            if (lower.contains("wool")) return valueOf(m, "wool");
            if (lower.equals("crafting_table")) return valueOf(m, "workbench");
            if (lower.contains("_door") && !lower.contains("iron_")) return valueOf(m, "wooden_door");
            if (lower.startsWith("wooden_")) return valueOf(m, lower.replace("wooden_", "wood"));
        }
        return material;
    }

    public static boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
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
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) return meta.getDisplayName();
        }
        String name = itemStack.getType().name();
        return CraftMaterial.getMaterial().getOrDefault(name, name);
    }
}
