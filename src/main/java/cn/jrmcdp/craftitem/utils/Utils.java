package cn.jrmcdp.craftitem.utils;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.CraftMaterial;
import cn.jrmcdp.craftitem.minigames.utils.LogUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("UnusedReturnValue")
public class Utils {
    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean createDirectory(File file) {
        return !file.exists() && file.mkdirs();
    }

    public static boolean createNewFile(File file) {
        if (file.exists()) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            LogUtils.warn("创建文件 " + file.getName() + " 时出现一个错误", e);
            return false;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void updateInventory(Player player) {
        player.updateInventory();
    }

    /**
     * CraftInventory#first(item, withAmount:false)
     */
    private static int first(Inventory inv, ItemStack item) {
        if (item == null) {
            return -1;
        } else {
            ItemStack[] inventory = inv.getContents(); // modified
            int i = 0;
            while (true) {
                if (i >= inventory.length) return -1;
                if (inventory[i] != null && item.isSimilar(inventory[i])) break;
                ++i;
            }
            return i;
        }
    }

    /**
     * 重写 CraftInventory#removeItem，解决材料在副手不消耗问题
     */
    public static void takeItem(Player player, ItemStack... items) {
        PlayerInventory inv = player.getInventory();
        HashMap<Integer, ItemStack> leftover = new HashMap<>();

        for (int i = 0; i < items.length; ++i) {
            ItemStack item = items[i];
            Preconditions.checkArgument(item != null, "ItemStack cannot be null");
            int toDelete = item.getAmount();

            while (true) {
                int first = first(inv, item); // modified
                if (first == -1) {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                }

                ItemStack itemStack = inv.getItem(first);
                if (itemStack == null) continue;
                int amount = itemStack.getAmount();
                if (amount <= toDelete) {
                    toDelete -= amount;
                    inv.setItem(first, null);
                } else {
                    itemStack.setAmount(amount - toDelete);
                    inv.setItem(first, itemStack);
                    toDelete = 0;
                }
                if (toDelete <= 0) break;
            }
        }
        if (!leftover.isEmpty()) {
            Logger logger = CraftItem.getPlugin().getLogger();
            logger.warning("预料中的问题，在扣除玩家 " + player.getName() + " 的所有材料时，有以下材料没有成功扣除");
            logger.warning("(格式: 物品所在格子索引 -- 物品ID x 物品数量)");
            for (Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
                ItemStack item = entry.getValue();
                logger.warning(entry.getKey() + " -- " + (item == null ? "null" : (item.getType()  + " x " + item.getAmount())));
            }
        }
    }

    public static ItemStack getItemStack(Material material, String name, List<String> lore) {
        return AdventureItemStack.buildItem(material, name, lore);
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
            if (lower.equals("iron_bars")) return valueOf(m, "iron_fence");

        }
        return material;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Optional<T> valueOf(Class<T> clazz, String s) {
        if (s != null && !s.isEmpty()) {
            if (clazz.isEnum()) {
                for (T t : clazz.getEnumConstants()) {
                    if (((Enum<?>) t).name().equalsIgnoreCase(s)) return Optional.of(t);
                }
            } else {
                Registry<?> registry = clazz.equals(Sound.class) ? Registry.SOUNDS
                        : clazz.equals(Material.class) ? Registry.MATERIAL
                        : null;
                if (registry != null) {
                    Keyed matched = registry.match(s.toUpperCase());
                    if (matched != null) {
                        return Optional.of((T) matched);
                    }
                }
            }
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
