package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.ColorHelper;
import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.Utils;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.holder.ForgeHolder;

import java.util.*;

import cn.jrmcdp.craftitem.minigames.utils.Pair;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Gui {
    private static YamlConfiguration config;

    private static String title;

    private static char[] chest;

    private static char[] chestTime;

    public static final Map<String, Icon> items = new HashMap<>();

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

    public static void reload() {
        config = FileConfig.Gui.getConfig();
        title = ColorHelper.parseColor(config.getString("Title"));

        chest = String.join("", config.getStringList("Chest")).toCharArray();
        chestTime = String.join("", config.getStringList("ChestTime")).toCharArray();

        items.clear();
        ConfigurationSection section = config.getConfigurationSection("Item");
        if (section != null) for (String key : section.getKeys(false)) {
            XMaterial xItem = XMaterial.matchXMaterial(section.getString(key + ".Type", "STONE")).orElse(null);
            org.bukkit.Material material = xItem == null ? null : xItem.parseMaterial();
            if (material == null) {
                continue;
            }
            String name = ColorHelper.parseColor(section.getString(key + ".Name"));
            int amount = section.getInt(key + ".Amount", 1);
            List<String> lore = ColorHelper.parseColor(section.getStringList(key + ".Lore"));
            Integer customModelData = section.contains(key + ".CustomModelData") ? section.getInt(key + ".CustomModelData") : null;
            List<String> leftClick = ColorHelper.parseColor(section.getStringList(key + ".LeftClick"));
            List<String> rightClick = ColorHelper.parseColor(section.getStringList(key + ".RightClick"));
            List<String> shiftLeftClick = ColorHelper.parseColor(section.getStringList(key + ".ShiftLeftClick"));
            List<String> shiftRightClick = ColorHelper.parseColor(section.getStringList(key + ".ShiftRightClick"));
            items.put(key, new Icon(key, material, amount, name, lore, customModelData, leftClick, rightClick, shiftLeftClick, shiftRightClick));
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
        Player player = playerData.getPlayer();
        ItemStack[] is = new ItemStack[holder.chest.length];
        Iterator<ItemStack> iterator = craftData.getMaterial().iterator();
        for (int i = 0; i < holder.chest.length; i++) {
            String key = String.valueOf(holder.chest[i]);
            switch (key) {
                case "材": {
                    if (iterator.hasNext()) {
                        is[i] = iterator.next();
                        break;
                    }
                    Icon icon = items.get(key);
                    if (icon != null) {
                        is[i] = icon.getItem(player);
                    }
                    break;
                }
                case "物": {
                    ItemStack item = craftData.getDisplayItem().clone();
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.getLore();
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
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    is[i] = item;
                    break;
                }
                case "锻": {
                    Icon icon = items.get(craftData.isDifficult() ? "锻_困难" : "锻");
                    if (icon != null) {
                        is[i] = icon.getItem(
                                player,
                                Pair.of("<ChanceName>", Config.getChanceName(craftData.getChance())),
                                Pair.of("<Score>", playerData.getScore(id)),
                                Pair.of("<Cost>", craftData.getCost())
                        );
                    }
                    break;
                }
                case "时": {
                    holder.putTimeSlot(i);
                    is[i] = holder.getTimeIcon();
                    break;
                }
                default : {
                    Icon icon = items.get(key);
                    if (icon != null) {
                        is[i] = icon.getItem(player);
                    }
                    break;
                }
            }
        }
        gui.setContents(is);
        return gui;
    }
}
