package cn.jrmcdp.craftitem.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static cn.jrmcdp.craftitem.utils.MiniMessageConvert.miniMessage;

public class PaperInventoryFactory implements InventoryFactory {
    @Override
    public Inventory create(InventoryHolder owner, int size, String title) {
        return Bukkit.createInventory(owner, size, miniMessage(title));
    }
}
