package cn.jrmcdp.craftitem.event;

import cn.jrmcdp.craftitem.data.CraftData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MaterialDisappearEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    boolean cancelled = false;
    Player player;
    CraftData craftData;
    ItemStack item;
    ItemStack itemToDisappear;

    public MaterialDisappearEvent(Player player, CraftData craftData, ItemStack item, ItemStack itemToDisappear) {
        this.player = player;
        this.craftData = craftData;
        this.item = item;
        this.itemToDisappear = itemToDisappear;
    }

    public Player getPlayer() {
        return player;
    }

    public CraftData getCraftData() {
        return craftData;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack getItemToDisappear() {
        return itemToDisappear;
    }

    public void setItemToDisappear(ItemStack itemToDisappear) {
        this.itemToDisappear = itemToDisappear;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
