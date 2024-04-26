package cn.jrmcdp.craftitem.listener;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.holder.IHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

public class GuiListener implements Listener {
    CraftItem plugin;
    BukkitTask timer;
    public GuiListener(CraftItem plugin) {
        this.plugin = plugin;
        this.timer = Bukkit.getScheduler().runTaskTimer(plugin, this::onSecond, 20L, 20L);
    }

    public void onSecond() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (holder instanceof IHolder) {
                ((IHolder) holder).onSecond();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof IHolder) {
            event.setCancelled(true);
            ((IHolder) holder).onClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof IHolder) {
            event.setCancelled(true);
        }
    }

    public void onDisable() {
        // 放置热重载时玩家可以拿取界面物品
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof IHolder) {
                player.closeInventory();
            }
        }
        if (timer != null) timer.cancel();
    }
}
