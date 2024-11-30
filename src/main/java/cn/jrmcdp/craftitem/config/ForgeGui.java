package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.ColorHelper;
import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Icon;
import cn.jrmcdp.craftitem.utils.AdventureItemStack;
import cn.jrmcdp.craftitem.utils.Utils;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.holder.ForgeHolder;

import java.util.*;
import java.util.logging.Logger;

import cn.jrmcdp.craftitem.utils.Pair;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ForgeGui {
    private static String title;

    private static char[] chest;

    private static char[] chestTime;

    public static final Map<String, Icon> items = new HashMap<>();

    public static char[] getChest() {
        return chest;
    }

    public static char[] getChestTime() {
        return chestTime;
    }

    public static void reload() {
        YamlConfiguration config = FileConfig.Gui.loadConfig();
        title = ColorHelper.parseColor(config.getString("Title"));

        chest = String.join("", config.getStringList("Chest")).toCharArray();
        chestTime = String.join("", config.getStringList("ChestTime")).toCharArray();

        items.clear();
        List<String> necessaryItems = Lists.newArrayList(
                "材", "物", "锻", "锻_连击", "锻_困难",
                "时", "时_未开启", "时_条件不足", "时_进行中", "时_完成"
        );
        ConfigurationSection section = config.getConfigurationSection("Item");
        if (section != null) for (String key : section.getKeys(false)) {
            String rawMaterial = section.getString(key + ".Type", "STONE");
            Material material = Utils
                    .parseMaterial(rawMaterial)
                    .orElse(null);
            if (material == null) {
                if (necessaryItems.contains(key)) {
                    material = Material.STONE;
                    CraftItem.getPlugin().getLogger().warning("Gui.yml 找不到图标 " + key + " 设定的物品类型 " + rawMaterial + "，使用默认图标");
                } else {
                    CraftItem.getPlugin().getLogger().warning("Gui.yml 找不到图标 " + key + " 设定的物品类型 " + rawMaterial);
                    continue;
                }
            }

            String name = ColorHelper.parseColor(section.getString(key + ".Name"));
            int data = section.getInt(key + ".Data", 0);
            int amount = section.getInt(key + ".Amount", 1);
            List<String> lore = ColorHelper.parseColor(section.getStringList(key + ".Lore"));
            Integer customModelData = section.contains(key + ".CustomModelData") ? section.getInt(key + ".CustomModelData") : null;
            List<String> leftClick = ColorHelper.parseColor(section.getStringList(key + ".LeftClick"));
            List<String> rightClick = ColorHelper.parseColor(section.getStringList(key + ".RightClick"));
            List<String> shiftLeftClick = ColorHelper.parseColor(section.getStringList(key + ".ShiftLeftClick"));
            List<String> shiftRightClick = ColorHelper.parseColor(section.getStringList(key + ".ShiftRightClick"));
            items.put(key, new Icon(material, data, amount, name, lore, customModelData, leftClick, rightClick, shiftLeftClick, shiftRightClick));
        }

        checkError(chest, "Chest");
        checkError(chestTime, "ChestTime");
    }

    private static void checkError(char[] inv, String name) {
        Logger logger = CraftItem.getPlugin().getLogger();
        if (inv.length % 9 != 0) {
            logger.warning("Gui.yml 配置有误: " + name + " 的长度 (" + inv.length + ") 不为 9 的倍数。如果你没有修改过这个选项，这可能由编码错误引起。");
            return;
        }
        for (int i = 0; i < inv.length; i++) {
            char c = inv[i];
            String key = String.valueOf(c);
            if (!items.containsKey(key)) {
                inv[i] = '　';
                logger.warning("Gui.yml 配置有误: 无法找到图标 '" + key + "'");
            }
        }
    }

    public static void openGui(PlayerData playerData, String id, CraftData craftData) {
        Bukkit.getScheduler().runTaskAsynchronously(CraftItem.getPlugin(), () -> {
            Inventory inventory = buildGui(playerData, id, craftData);
            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> playerData.getPlayer().openInventory(inventory));
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
                    List<String> lore = AdventureItemStack.getItemLoreAsMiniMessage(item);
                    if (lore == null) lore = new ArrayList<>();
                    lore.addAll(Message.gui__craft_info__lore__header.list());
                    for (ItemStack itemStack : craftData.getItems())
                        lore.add(Message.gui__craft_info__lore__item.get(Utils.getItemName(itemStack), itemStack.getAmount()));
                    for (String command : craftData.getCommands()) {
                        String[] split = command.split("\\|\\|");
                        if (split.length > 1) lore.add(Message.gui__craft_info__lore__command.get(split[1]));
                    }
                    AdventureItemStack.setItemLore(item, lore);
                    is[i] = item;
                    break;
                }
                case "锻": {
                    Icon icon = items.get(craftData.isDifficult()
                            ? "锻_困难"
                            : (craftData.getCombo() > 0 ? "锻_连击" : "锻"));
                    if (icon != null) {
                        int count = playerData.getForgeCount(id);
                        int limit = craftData.getForgeCountLimit(player);
                        is[i] = icon.getItem(
                                player,
                                Pair.of("<ChanceName>", Config.getChanceName(craftData.getChance())),
                                Pair.of("<Score>", playerData.getScore(id)),
                                Pair.of("<Cost>", craftData.getCost()),
                                Pair.of("<Combo>", craftData.getCombo()),
                                Pair.of("<LimitCountCurrent>", count),
                                Pair.of("<LimitCountMax>", limit != 0 ? Math.max(limit, 0) : Message.craft__unlimited.get()),
                                Pair.of("<LimitCount>", limit != 0 ? Message.craft__limited.get(count, limit) : Message.craft__unlimited.get())
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
