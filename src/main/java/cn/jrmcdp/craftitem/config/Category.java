package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.utils.Utils;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.holder.CategoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Category {
    private static String title;

    private static String[] chest;

    private static HashMap<String, ItemStack> items;

    private static int slotAmount;

    public static int getSlotAmount() {
        return slotAmount;
    }

    public static void reload() {
        YamlConfiguration config = FileConfig.Category.loadConfig();
        title = config.getString("Title");
        chest = new String[config.getStringList("Chest").size() * 9];
        StringBuilder info = new StringBuilder();
        for (String line : config.getStringList("Chest")) {
            info.append(line);
        }
        chest = info.toString().split("");
        slotAmount = 0;
        for (String key : chest) {
            if (key.equals("方")) {
                slotAmount++;
            }
        }
        items = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("Item");
        if (section != null) for (String key : section.getKeys(false)) {
            ItemStack itemStack = Utils
                    .parseMaterial(section.getString(key + ".Type", "STONE"))
                    .map(ItemStack::new)
                    .orElseGet(() -> new ItemStack(Material.STONE));
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                if (section.get(key + ".Name") != null) {
                    meta.setDisplayName(section.getString(key + ".Name"));
                }
                meta.setLore(section.getStringList(key + ".Lore"));
            }
            itemStack.setItemMeta(meta);
            items.put(key, itemStack);
        }
    }

    public static void openGui(PlayerData playerData, String type, List<String> craftList, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(CraftItem.getPlugin(), () -> {
            CategoryHolder holder = buildGui(playerData, type, craftList, page);
            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> playerData.getPlayer().openInventory(holder.getInventory()));
        });
    }

    public static CategoryHolder buildGui(PlayerData playerData, String type, List<String> craftList, int page) {
        CategoryHolder holder = new CategoryHolder(chest, playerData, type, craftList, page);
        Inventory gui = Bukkit.createInventory(holder, chest.length, title.replace("<Category>", type));
        holder.setInventory(gui);
        ItemStack[] is = new ItemStack[chest.length];
        Iterator<String> iterator = craftList.subList(
                Math.min(craftList.size(), page * slotAmount),
                Math.min(craftList.size(), page * slotAmount + slotAmount)
        ).iterator();
        for (int i = 0; i < chest.length; i++) {
            String key = chest[i];
            if (key.equals("方")) {
                if (!iterator.hasNext()) {
                    is[i] = null;
                    continue;
                }
                String name = iterator.next();
                CraftData craftData = Craft.getCraftData(name);
                if (craftData == null) {
                    ItemStack itemStack = new ItemStack(Material.PAPER);
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) meta.setDisplayName("§c未找到 §e" + name);
                    itemStack.setItemMeta(meta);
                    is[i] = itemStack;
                    continue;
                }
                ItemStack clone = craftData.getDisplayItem().clone();
                ItemMeta meta = clone.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore == null) lore = new ArrayList<>();
                    lore.add("");
                    lore.add("§a包含:");
                    for (ItemStack itemStack : craftData.getItems())
                        lore.add(" §8➥ §e" + Utils.getItemName(itemStack) + "§fx" + itemStack.getAmount());
                    for (String command : craftData.getCommands()) {
                        String[] split = command.split("\\|\\|");
                        if (split.length > 1) lore.add(" §8➥ §e" + command.split("\\|\\|")[1]);
                    }
                    meta.setLore(lore);
                }
                clone.setItemMeta(meta);
                is[i] = clone;
                holder.getSlot()[i] = name;
            } else {
                is[i] = items.get(key);
            }
        }
        gui.setContents(is);
        return holder;
    }
}
