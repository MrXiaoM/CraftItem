package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.holder.CategoryHolder;
import cn.jrmcdp.craftitem.utils.AdventureItemStack;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
            if (!itemStack.getType().equals(Material.AIR)) {
                String name = section.getString(key + ".Name");
                List<String> lore = section.getStringList(key + ".Lore");
                if (name != null) AdventureItemStack.setItemDisplayName(itemStack, name);
                if (!lore.isEmpty()) AdventureItemStack.setItemLore(itemStack, lore);
            }
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
        Inventory gui = CraftItem.getInventoryFactory().create(holder, chest.length, title.replace("<Category>", type));
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
                    is[i] = AdventureItemStack.buildItem(Material.PAPER, Message.gui__category__not_found.get(name), null);
                    continue;
                }
                ItemStack clone = craftData.getDisplayItem().clone();
                List<String> lore = AdventureItemStack.getItemLoreAsMiniMessage(clone);
                if (lore == null) lore = new ArrayList<>();
                lore.addAll(Message.gui__craft_info__lore__header.list());
                for (ItemStack itemStack : craftData.getItems())
                    lore.add(Message.gui__craft_info__lore__item.get(Utils.getItemName(itemStack), itemStack.getAmount()));
                for (String command : craftData.getCommands()) {
                    String[] split = command.split("\\|\\|");
                    if (split.length > 1) lore.add(Message.gui__craft_info__lore__command.get(split[1]));
                }
                AdventureItemStack.setItemLore(clone, lore);
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
