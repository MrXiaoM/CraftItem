package cn.jrmcdp.craftitem.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static cn.jrmcdp.craftitem.utils.MiniMessageConvert.miniMessageToLegacy;

public class BukkitInventoryFactory implements InventoryFactory {
    @Override
    public Inventory create(InventoryHolder owner, int size, String title) {
        return Bukkit.createInventory(owner, size, miniMessageToLegacy(title.startsWith("&") ? title : ("&0" + title)));
    }
}
