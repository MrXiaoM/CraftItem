package cn.jrmcdp.craftitem.utils;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ItemTranslation;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.func.entry.adapter.IMaterialAdapter;
import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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

    public static InventoryHolder getHolder(Inventory inv) {
        try {
            return inv.getHolder();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * CraftInventory#first(item, withAmount:false)
     */
    private static int first(Inventory inv, IMaterialAdapter item) {
        if (item == null) {
            return -1;
        } else {
            ItemStack[] inventory = inv.getContents(); // modified
            int i = 0;
            while (true) {
                if (i >= inventory.length) return -1;
                if (inventory[i] != null && item.match(inventory[i])) break;
                ++i;
            }
            return i;
        }
    }

    /**
     * 重写 CraftInventory#removeItem，解决材料在副手不消耗问题
     */
    public static void takeItem(Player player, List<MaterialInstance> list) {
        PlayerInventory inv = player.getInventory();
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        List<MaterialInstance.Mutable> items = MaterialInstance.toMutable(list);

        for (int i = 0; i < items.size(); ++i) {
            MaterialInstance.Mutable item = items.get(i);
            Preconditions.checkArgument(item != null, "ItemStack cannot be null");
            int toDelete = item.getAmount();

            while (true) {
                int first = first(inv, item.getAdapter()); // modified
                if (first == -1) {
                    item.setAmount(toDelete);
                    leftover.put(i, item.getSample());
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

    public static List<String> itemToListString(Collection<ItemStack> collection, Player player) {
        List<String> list = new ArrayList<>();
        for (ItemStack itemStack : collection) {
            list.add("§a" + Utils.getItemName(itemStack, player) + "§fx" + itemStack.getAmount());
        }
        return list;
    }

    public static Map<MaterialInstance, Integer> getAmountMap(List<MaterialInstance> list) {
        Map<MaterialInstance, Integer> map = new HashMap<>();
        for (MaterialInstance material : list) {
            MaterialInstance key = material.forCounter();
            int amount = material.getSample().getAmount();

            map.put(key, map.getOrDefault(key, 0) + amount);
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
                    } else {
                        matched = registry.match(s.replace("_", "."));
                        if (matched != null) {
                            return Optional.of((T) matched);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static <T> T valueOfOrNull(Class<T> type, String s, String... def) {
        Optional<T> value = valueOf(type, s);
        if (value.isPresent()) return value.get();
        for (String string : def) {
            Optional<T> v = valueOf(type, s);
            if (v.isPresent()) return v.get();
        }
        return null;
    }

    public static String getItemName(ItemStack itemStack, Player player) {
        if (itemStack == null) return "空";
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) return meta.getDisplayName();
        }
        return ItemTranslation.get(itemStack, player);
    }
}
