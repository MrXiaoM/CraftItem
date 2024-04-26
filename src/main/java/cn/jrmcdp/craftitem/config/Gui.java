package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.Utils;
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

    private static char[] chest;

    private static char[] chestTime;

    private static final HashMap<String, ItemStack> items = new HashMap<>();

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static String getTitle() {
        return title;
    }

    public static char[] getChest() {
        return chest;
    }

    public static char[] getChestTime() {
        return chestTime;
    }

    public static HashMap<String, ItemStack> getItems() {
        return items;
    }

    public static void reload() {
        config = FileConfig.Gui.getConfig();
        title = config.getString("Title");

        chest = String.join("", config.getStringList("Chest")).toCharArray();
        chestTime = String.join("", config.getStringList("ChestTime")).toCharArray();

        items.clear();
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
        Inventory gui = ForgeHolder.buildGui(playerData, id, craftData, chest.length, title);
        ForgeHolder holder = (ForgeHolder) gui.getHolder();
        if (holder == null) return gui;
        ItemStack[] is = new ItemStack[holder.chest.length];
        Iterator<ItemStack> iterator = craftData.getMaterial().iterator();
        for (int i = 0; i < holder.chest.length; i++) {
            ItemStack clone, item;
            ItemMeta itemMeta;
            List<String> lore;
            int j, loreSize;
            String key = String.valueOf(holder.chest[i]);
            switch (key) {
                case "材": {
                    if (iterator.hasNext()) {
                        is[i] = iterator.next();
                        break;
                    }
                    is[i] = getItems().get(key);
                    break;
                }
                case "物": {
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
                    break;
                }
                case "锻": {
                    item = getItems().get(craftData.isDifficult() ? "锻_困难" : "锻").clone();
                    itemMeta = item.getItemMeta();
                    lore = itemMeta.getLore();
                    for (j = 0, loreSize = (lore == null ? 0 : lore.size()); j < loreSize; j++) {
                        String line = lore.get(j);
                        if (line.contains("<ChanceName>"))
                            line = line.replace("<ChanceName>", Config.getChanceName(craftData.getChance()));
                        if (line.contains("<Score>"))
                            line = line.replace("<Score>", String.valueOf(playerData.getScore(id)));
                        if (line.contains("<Cost>"))
                            line = line.replace("<Cost>", String.valueOf(craftData.getCost()));
                        lore.set(j, line);
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    is[i] = item;
                    break;
                }
                case '时': {
                    Long endTime = holder.endTime;
                    if (craftData.getTime() > 0) {
                        if (Config.isMeetTimeForgeCondition(playerData.getPlayer())) {
                            if (holder.done) {
                                item = items.get("时_完成").clone();
                            } else if (holder.processing) {
                                item = items.get("时_进行中").clone();
                            } else {
                                item = items.get("时").clone();
                            }
                        } else {
                            item = items.get("时_条件不足").clone();
                        }
                    } else {
                        item = items.get("时_未开启").clone();
                    }
                    double progress = endTime == null ? 0.0d
                            : Math.min(1.0d, (System.currentTimeMillis() - endTime - craftData.getTime() * 1000.0d) / (craftData.getTime() * 1000.0d));
                    itemMeta = item.getItemMeta();
                    lore = itemMeta.getLore();
                    for (j = 0, loreSize = lore == null ? 0 : lore.size(); j < loreSize; j++) {
                        String line = lore.get(j);
                        if (line.contains("<Progress>"))
                            line = line.replace("<Progress>", String.format("%.2f", progress * 100));
                        if (line.contains("<RemainTime>"))
                            line = line.replace("<RemainTime>", endTime == null ? ""
                                    : CraftData.getTimeDisplay(Math.max(0, (endTime - System.currentTimeMillis()) / 1000L)));
                        if (line.contains("<Time>"))
                            line = line.replace("<Time>", craftData.getTimeDisplay());
                        if (line.contains("<Cost>"))
                            line = line.replace("<Cost>", String.valueOf(craftData.getTimeCost()));
                        lore.set(j, line);
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    is[i] = item;
                }
                default : {
                    is[i] = items.get(String.valueOf(key));
                    break;
                }
            }
        }
        gui.setContents(is);
        return gui;
    }
}
