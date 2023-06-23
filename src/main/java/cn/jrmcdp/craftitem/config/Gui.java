package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.holder.ForgeHolder;

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

public class Gui {
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
        config = FileConfig.Gui.getConfig();
        title = config.getString("Title");
        chest = new String[config.getStringList("Chest").size() * 9];
        String info = "";
        for (String line : config.getStringList("Chest")) {
            info = info + line;
        }
        chest = info.split("");
        slotAmount = 0;
        for (String key : chest) {
            if (key.equals("材")) {
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

    public static void openGui(PlayerData playerData, String id, CraftData craftData) {
        Bukkit.getScheduler().runTaskAsynchronously(CraftItem.getPlugin(), () -> {
            Inventory inventory = buildGui(playerData, id, craftData);
            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> {playerData.getPlayer().openInventory(inventory);});
        });
    }

    public static Inventory buildGui(PlayerData playerData, String id, CraftData craftData) {
        ForgeHolder holder = new ForgeHolder(playerData, id, craftData);
        Inventory gui = Bukkit.createInventory(holder, chest.length, title);
        ItemStack[] is = new ItemStack[chest.length];
        Iterator<ItemStack> iterator = craftData.getMaterial().iterator();
        for (int i = 0; i < chest.length; i++) {
            ItemStack clone, item;
            ItemMeta itemMeta;
            List<String> lore;
            int j, loreSize;
            String key = chest[i];
            switch (key) {
                case "材" : {
                    if (iterator.hasNext()) {
                        is[i] = iterator.next();
                        break;
                    }
                    is[i] = items.get(key);
                    break;
                }
                case "物" : {
                    clone = craftData.getDisplayItem().clone();
                    itemMeta = clone.getItemMeta();
                    lore = itemMeta.getLore();
                    if (lore == null)
                        lore = new ArrayList<>();
                    lore.add("");
                    lore.add("§a包含:");
                    for (ItemStack itemStack : craftData.getItems())
                        lore.add(" §8➥ §e" + Material.getItemName(itemStack) + "§fx" + itemStack.getAmount());
                    for (String command : craftData.getCommands()) {
                        String[] split = command.split("\\|\\|");
                        if (split.length > 1) lore.add(" §8➥ §e" + command.split("\\|\\|")[1]);
                    }
                    itemMeta.setLore(lore);
                    clone.setItemMeta(itemMeta);
                    is[i] = clone;
                    break;
                }
                case "锻" : {
                    item = items.get(key).clone();
                    itemMeta = item.getItemMeta();
                    lore = itemMeta.getLore();
                    for (j = 0, loreSize = lore.size(); j < loreSize; j++) {
                        String line = lore.get(j);
                        if (line.contains("<ChanceName>"))
                            lore.set(j, line.replace("<ChanceName>", Config.getChanceName(craftData.getChance())));
                        if (line.contains("<Score>"))
                            lore.set(j, line.replace("<Score>", "" + playerData.getScore(id)));
                        if (line.contains("<Cost>"))
                            lore.set(j, line.replace("<Cost>", "" + craftData.getCost()));
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    is[i] = item;
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
