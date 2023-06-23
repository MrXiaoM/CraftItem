package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.Utils;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.holder.CategoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Category {
    private static YamlConfiguration config;

    private static String title;

    private static String[] chest;

    private static HashMap<String, ItemStack> items;

    private static int slotAmount;

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static String getTitle() {
        return title;
    }

    public static String[] getChest() {
        return chest;
    }

    public static HashMap<String, ItemStack> getItems() {
        return items;
    }

    public static int getSlotAmount() {
        return slotAmount;
    }

    public static void reload() {
        config = FileConfig.Category.getConfig();
        title = config.getString("Title");
        chest = new String[config.getStringList("Chest").size() * 9];
        String info = "";
        for (String line : config.getStringList("Chest")) {
            info = info + line;
        }
        chest = info.split("");
        slotAmount = 0;
        for (String key : chest) {
            if (key.equals("方")) {
                slotAmount++;
            }
        }
        items = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("Item");
        for (String key : section.getKeys(false)) {
            ItemStack itemStack = XMaterial.matchXMaterial(section.getString(key + ".Type", "STONE")).get().parseItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (section.get(key + ".Name") != null) {
                itemMeta.setDisplayName(section.getString(key + ".Name"));
            }
            itemMeta.setLore(section.getStringList(key + ".Lore"));
            itemStack.setItemMeta(itemMeta);
            items.put(key, itemStack);
        }
    }

    public static void openGui(PlayerData playerData, String type, List<String> craftList, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(CraftItem.getPlugin(), () -> {
            Inventory inventory = buildGui(playerData, type, craftList, page);
            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> {playerData.getPlayer().openInventory(inventory);});
        });
    }

    public static Inventory buildGui(PlayerData playerData, String type, List<String> craftList, int page) {
        CategoryHolder holder = new CategoryHolder(chest, playerData, type, craftList, page);
        Inventory gui = Bukkit.createInventory(holder, chest.length, title.replace("<Category>", type));
        holder.setInventory(gui);
        ItemStack[] is = new ItemStack[chest.length];
        Iterator<String> iterator = craftList.subList(Math.min(craftList.size(), page*slotAmount), Math.min(craftList.size(), page*slotAmount+slotAmount)).iterator();
        for (int i = 0; i < chest.length; i++) {
            ItemStack clone;
            ItemMeta itemMeta;
            List<String> lore;
            String key = chest[i];
            switch (key) {
                case "方" : {
                    if (iterator.hasNext()) {
                        String name = iterator.next();
                        CraftData craftData = Craft.getCraftData(name);
                        if (craftData == null) {
                            ItemStack itemStack = XMaterial.PAPER.parseItem();
                            ItemMeta meta = itemStack.getItemMeta();
                            meta.setDisplayName("§c未找到 §e" + name);
                            itemStack.setItemMeta(meta);
                            is[i] = itemStack;
                            break;
                        }
                        clone = craftData.getDisplayItem().clone();
                        itemMeta = clone.getItemMeta();
                        lore = itemMeta.getLore();
                        if (lore == null)
                            lore = new ArrayList<>();
                        lore.add("");
                        lore.add("§a包含:");
                        for (ItemStack itemStack : craftData.getItems())
                            lore.add(" §8➥ §e" + Utils.getItemName(itemStack) + "§fx" + itemStack.getAmount());
                        for (String command : craftData.getCommands()) {
                            String[] split = command.split("\\|\\|");
                            if (split.length > 1) lore.add(" §8➥ §e" + command.split("\\|\\|")[1]);
                        }
                        itemMeta.setLore(lore);
                        clone.setItemMeta(itemMeta);
                        is[i] = clone;
                        holder.getSlot()[i] = name;
                    } else {
                        is[i] = null;
                    }
                    break;
                }
                default : {
                    is[i] = items.get(key);
                    break;
                }
            }
        }
        gui.setContents(is);
        return gui;
    }

}
