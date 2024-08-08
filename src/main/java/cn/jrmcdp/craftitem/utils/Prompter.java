package cn.jrmcdp.craftitem.utils;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Prompter implements InventoryHolder {
    Inventory inventory;
    private Prompter() {}

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void gui(Player player, int size, Message title, Consumer<Inventory> consumer) {
        gui(player, size, title, null, null, consumer);
    }
    public static void gui(Player player, int size, Message title, ItemStack[] items, Consumer<Inventory> consumer) {
        gui(player, size, title, inv -> inv.addItem(items), null, consumer);
    }
    public static void gui(Player player, int size, Message title, List<ItemStack> items, Consumer<Inventory> consumer) {
        gui(player, size, title, inv -> {
            for (ItemStack item : items) {
                inv.addItem(item);
            }
        }, null, consumer);
    }
    public static void gui(Player player, int size, Message title, Consumer<Inventory> items, Consumer<InventoryClickEvent> click, Consumer<Inventory> close) {
        gui(player, size, title, items, click, inv -> {
            close.accept(inv);
            return true;
        });
    }
    public static void gui(Player player, int size, Message title, Consumer<Inventory> items, Consumer<InventoryClickEvent> click, Function<Inventory, Boolean> close) {
        Prompter holder = new Prompter();
        holder.inventory = Bukkit.createInventory(holder, size, title.get());
        if (items != null) items.accept(holder.inventory);
        player.openInventory(holder.inventory);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent e) {
                if (e.getWhoClicked().getName().equals(player.getName())) {
                    if (click != null) {
                        click.accept(e);
                    }
                }
            }
            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (event.getPlayer().equals(player)) {
                    if (close.apply(event.getInventory())) {
                        HandlerList.unregisterAll(this);
                    }
                }
            }
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                HandlerList.unregisterAll(this);
            }
        }, CraftItem.getPlugin());
    }

    public static void onChat(Player player, Consumer<String> consumer) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onAsyncPlayerChat(AsyncPlayerChatEvent eventB) {
                if (eventB.getPlayer().equals(player)) {
                    eventB.setCancelled(true);
                    consumer.accept(eventB.getMessage());
                    HandlerList.unregisterAll(this);
                }
            }
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                HandlerList.unregisterAll(this);
            }
        }, CraftItem.getPlugin());
    }

}
