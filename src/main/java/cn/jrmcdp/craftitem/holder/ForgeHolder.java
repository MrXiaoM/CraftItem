package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ForgeHolder implements InventoryHolder {
    private PlayerData playerData;

    private String id;

    private CraftData craftData;

    public ForgeHolder(PlayerData playerData, String id, CraftData craftData) {
        this.playerData = playerData;
        this.id = id;
        this.craftData = craftData;
    }

    public PlayerData getPlayerData() {
        return this.playerData;
    }

    public void setPlayerData(PlayerData playerData) {
        this.playerData = playerData;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CraftData getCraftData() {
        return this.craftData;
    }

    public void setCraftData(CraftData craftData) {
        this.craftData = craftData;
    }

    public Inventory getInventory() {
        return null;
    }
}
