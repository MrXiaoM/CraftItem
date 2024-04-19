package cn.jrmcdp.craftitem.event;

import cn.jrmcdp.craftitem.holder.ForgeHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CraftSuccessEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    private final Player player;
    private final ForgeHolder holder;
    private final int oldValue;
    private int newValue;
    private int multiple;
    public CraftSuccessEvent(Player player, ForgeHolder holder, int oldValue, int newValue, int multiple) {
        this.player = player;
        this.holder = holder;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.multiple = multiple;
    }

    public Player getPlayer() {
        return player;
    }

    public ForgeHolder getHolder() {
        return holder;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }

    public void setNewValue(int newValue) {
        this.newValue = newValue;
    }

    /**
     * 获取成功类型，0小成功，1成功，2大成功
     */
    public int getMultiple() {
        return multiple;
    }

    /**
     * 设置成功类型，0小成功，1成功，2大成功。
     * 设为这之外的值将不会发送成功提示
     */
    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }
}
