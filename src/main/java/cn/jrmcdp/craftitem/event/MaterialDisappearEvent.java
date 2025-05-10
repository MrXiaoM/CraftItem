package cn.jrmcdp.craftitem.event;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.MaterialInstance;
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
    private boolean cancelled = false;
    private final Player player;
    private final CraftData craftData;
    private final ItemStack item;
    private final MaterialInstance material;
    private ItemStack itemToDisappear;
    private MaterialInstance materialToDisappear;

    public MaterialDisappearEvent(Player player, CraftData craftData, MaterialInstance material, MaterialInstance materialToDisappear) {
        this.player = player;
        this.craftData = craftData;
        this.material = material;
        this.materialToDisappear = materialToDisappear;
        this.item = material.getSample();
        this.itemToDisappear = materialToDisappear.getSample();
    }

    public Player getPlayer() {
        return player;
    }

    public CraftData getCraftData() {
        return craftData;
    }

    @Deprecated
    public ItemStack getItem() {
        return item;
    }

    @Deprecated
    public ItemStack getItemToDisappear() {
        return itemToDisappear;
    }

    @Deprecated
    public void setItemToDisappear(ItemStack itemToDisappear) {
        if (itemToDisappear == null) {
            setMaterialToDisappear(null);
        } else {
            String location = "<unknown>";
            for (StackTraceElement element : new Exception().getStackTrace()) {
                if (!element.getClassName().contains("cn.jrmcdp.craftitem")) {
                    location = element.toString();
                    break;
                }
            }
            CraftItem.getInstance().warn("MaterialDisappearEvent.setItemToDisappear 已弃用，调用这个方法不会产生任何效果。请改用 MaterialDisappearEvent.setMaterialToDisappear\n  at " + location);
        }
    }

    public MaterialInstance getMaterial() {
        return material;
    }

    public MaterialInstance getMaterialToDisappear() {
        return materialToDisappear;
    }

    public void setMaterialToDisappear(MaterialInstance materialToDisappear) {
        this.materialToDisappear = materialToDisappear;
        this.itemToDisappear = materialToDisappear == null ? null : materialToDisappear.getSample();
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
