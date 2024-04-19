package cn.jrmcdp.craftitem.listener;

import cn.jrmcdp.craftitem.holder.CategoryHolder;
import cn.jrmcdp.craftitem.holder.EditHolder;
import cn.jrmcdp.craftitem.holder.ForgeHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder == null)
            return;
        if (holder instanceof ForgeHolder) {
            event.setCancelled(true);
            ((ForgeHolder) holder).onClick(event);
        } else if (holder instanceof CategoryHolder) {
            event.setCancelled(true);
            ((CategoryHolder) holder).onClick(event);
        } else if (holder instanceof EditHolder) {
            event.setCancelled(true);
            ((EditHolder) holder).onClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder inventoryHolder = inventory.getHolder();
        if (inventoryHolder instanceof ForgeHolder || inventoryHolder instanceof CategoryHolder || inventoryHolder instanceof EditHolder) {
            event.setCancelled(true);
        }
    }
}
