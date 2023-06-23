package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.config.Category;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;

public class CategoryHolder implements InventoryHolder {
    private Inventory inventory;

    private String[] chest;
    private PlayerData playerData;
    private String type;
    private List<String> list;
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

    public String getType() {
        return type;
    }

    public List<String> getList() {
        return list;
    }

    public int getPage() {
        return page;
    }

    public String[] getSlot() {
        return slot;
    }

    public boolean upPage() {
        if (page < 1) return false;
        page--;
        Inventory gui = Category.buildGui(playerData, type, list, page);
        inventory.setContents(gui.getContents());
        slot = ((CategoryHolder)gui.getHolder()).getSlot();
        return true;
    }

    public boolean downPage() {
        if (list.subList(Math.min(list.size(), (page+1)*Category.getSlotAmount()), Math.min(list.size(), (page+1)*Category.getSlotAmount()+Category.getSlotAmount())).isEmpty()) return false;
        page++;
        Inventory gui = Category.buildGui(playerData, type, list, page);
        inventory.setContents(gui.getContents());
        slot = ((CategoryHolder)gui.getHolder()).getSlot();
        return true;
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
