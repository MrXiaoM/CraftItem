package cn.jrmcdp.craftitem.gui;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.func.CraftRecipeManager;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GuiCategory implements IHolder {
    private Inventory inventory;

    private final String title;
    private final String[] chest;
    private final Map<String, ItemStack> items;
    private final int slotAmount;

    private final PlayerData playerData;
    private final String type;
    private final List<String> craftList;
    private String[] slot;

    private int page;
    private final CraftRecipeManager manager = CraftRecipeManager.inst();
    private final ConfigCategoryGui parent;
    public GuiCategory(ConfigCategoryGui parent, String title, String[] chest, Map<String, ItemStack> items, int slotAmount, PlayerData playerData, String type, List<String> craftList, int page) {
        this.parent = parent;
        this.title = title;
        this.chest = chest;
        this.items = items;
        this.slotAmount = slotAmount;
        this.slot = new String[chest.length];
        this.playerData = playerData;
        this.type = type;
        this.craftList = manager.filter(getPlayer(), craftList);
        this.page = page;
    }

    public String[] getChest() {
        return chest;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public String[] getSlot() {
        return slot;
    }

    public boolean upPage() {
        if (page < 1) return false;
        return true;
    }

    public boolean downPage() {
        int size = craftList.size();
        int nextIndex = (page + 1) * parent.getSlotAmount();
        int startIndex = Math.min(size, nextIndex);
        int endIndex = Math.min(size, nextIndex + parent.getSlotAmount());
        if (craftList.subList(startIndex, endIndex).isEmpty()) return false;
        return true;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Player getPlayer() {
        return playerData.getPlayer();
    }

    @Override
    public Inventory newInventory() {
        inventory = parent.plugin.createInventory(this, chest.length, title.replace("<Category>", type));

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
                CraftData craftData = manager.getCraftData(name);
                if (craftData == null) {
                    is[i] = AdventureItemStack.buildItem(Material.PAPER, Message.gui__category__not_found.strFormat(name));
                    continue;
                }
                ItemStack clone = craftData.getDisplayItem().clone();
                ItemMeta meta = clone.getItemMeta();
                List<String> lore = meta == null ? null : meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.addAll(Message.gui__craft_info__lore__header.list());
                for (ItemStack itemStack : craftData.getItems())
                    lore.add(Message.gui__craft_info__lore__item.strFormat(Utils.getItemName(itemStack, getPlayer()), itemStack.getAmount()));
                for (String command : craftData.getCommands()) {
                    String[] split = command.split("\\|\\|");
                    if (split.length > 1) lore.add(Message.gui__craft_info__lore__command.strFormat(split[1]));
                }
                AdventureItemStack.setItemLoreMiniMessage(clone, lore);
                is[i] = clone;
                slot[i] = name;
            } else {
                is[i] = items.get(key);
            }
        }
        inventory.setContents(is);
        return inventory;
    }

    @Override
    public void onClick(
            InventoryAction action, ClickType click,
            InventoryType.SlotType slotType, int slot,
            ItemStack currentItem, ItemStack cursor,
            InventoryView view, InventoryClickEvent event
    ) {
        event.setCancelled(true);
        final Player player = (Player)event.getWhoClicked();
        if (slot < 0 || slot >= chest.length) return;
        parent.plugin.config().getSoundClickInventory().play(player);
        String key = chest[slot];
        switch (key) {
            case "上" : {
                if (!upPage()) {
                    Message.page__already_first.tm(player);
                    return;
                }
                page--;
                manager.plugin.getScheduler().runTask(this::open);
                return;
            }
            case "下" : {
                if (!downPage()) {
                    Message.page__already_last.tm(player);
                    return;
                }
                page++;
                manager.plugin.getScheduler().runTask(this::open);
                return;
            }
            case "方" : {
                String name = getSlot()[event.getRawSlot()];
                if (name == null) return;
                if (!manager.hasPermission(player, name)) {
                    Message.no_permission.tmf(getPlayer(), manager.getPermission(name));
                    return;
                }
                ConfigForgeGui.inst().openGui(getPlayerData(), name, manager.getCraftData(name), type);
            }
        }
    }
}
