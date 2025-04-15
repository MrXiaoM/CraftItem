package cn.jrmcdp.craftitem.utils;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ItemTranslation;
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
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("UnusedReturnValue")
public class Utils {
    public static List<String> replace(List<String> list, Pair<String, Object>... replacements) {
        return Pair.replace(list, replacements);
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
        for (String id : ids) {
            Material material = Material.getMaterial(id.toUpperCase());
            if (material != null) return material;
        }
        return Material.STONE;
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
            map.put(item, map.getOrDefault(item, 0) + itemStack.getAmount());
        }
        return map;
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
            if (lower.equals("red_dye")) return valueOf(m, "ink_sack");

        }
        return material;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Optional<T> valueOf(Class<T> type, String s) {
        if (s != null && !s.isEmpty()) {
            if (type.isEnum()) {
                for (T t : type.getEnumConstants()) {
                    if (((Enum<?>) t).name().equalsIgnoreCase(s)) return Optional.of(t);
                }
            } else {
                Registry<?> registry = type.equals(Sound.class) ? Registry.SOUNDS
                        : type.equals(Material.class) ? Registry.MATERIAL
                        : null;
                if (registry != null) {
                    Keyed matched = registry.match(s);
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
        return ItemTranslation.get(name);
    }
}
