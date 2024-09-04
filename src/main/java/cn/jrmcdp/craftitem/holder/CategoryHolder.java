package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryHolder implements IHolder {
    private Inventory inventory;

    private final String[] chest;
    private final PlayerData playerData;
    private final String type;
    private final List<String> list;
    private String[] slot;

    private int page;

    public CategoryHolder(String[] chest, PlayerData playerData, String type, List<String> list, int page) {
        this.chest = chest;
        this.playerData = playerData;
        this.type = type;
        this.list = list;
        this.page = page;
        this.slot = new String[54];
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
        CategoryHolder holder = Category.buildGui(playerData, type, list, --page);
        Inventory gui = holder.getInventory();
        inventory.setContents(gui.getContents());
        slot = holder.getSlot();
        return true;
    }

    public boolean downPage() {
        int size = list.size();
        int nextIndex = (page + 1) * Category.getSlotAmount();
        int startIndex = Math.min(size, nextIndex);
        int endIndex = Math.min(size, nextIndex + Category.getSlotAmount());
        if (list.subList(startIndex, endIndex).isEmpty()) return false;
        CategoryHolder holder = Category.buildGui(playerData, type, list, ++page);
        Inventory gui = holder.getInventory();
        inventory.setContents(gui.getContents());
        slot = holder.getSlot();
        return true;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void onClick(InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        if (event.getRawSlot() < 0 || event.getRawSlot() >= getChest().length) return;
        Config.playSoundClickInventory(player);
        String key = getChest()[event.getRawSlot()];
        switch (key) {
            case "上" : {
                if (!upPage()) Message.page__already_first.msg(player);
                return;
            }
            case "下" : {
                if (!downPage()) Message.page__already_last.msg(player);
                return;
            }
            case "方" : {
                String name = getSlot()[event.getRawSlot()];
                if (name == null) return;
                ForgeGui.openGui(getPlayerData(), name, Craft.getCraftData(name));
            }
        }
    }
}
