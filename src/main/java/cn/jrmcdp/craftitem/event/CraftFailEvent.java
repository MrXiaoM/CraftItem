package cn.jrmcdp.craftitem.event;

import cn.jrmcdp.craftitem.gui.GuiForge;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CraftFailEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    private final Player player;
    private final GuiForge holder;
    private final int oldValue;
    private int newValue;
    private int multiple;
    public CraftFailEvent(Player player, GuiForge holder, int oldValue, int newValue, int multiple) {
        this.player = player;
        this.holder = holder;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.multiple = multiple;
    }

    public Player getPlayer() {
        return player;
    }

    public GuiForge getHolder() {
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
     * 获取失败类型，0小失败，1失败，2大失败
     */
    public int getMultiple() {
        return multiple;
    }

    /**
     * 设置失败类型，0小失败，1失败，2大失败。
     * 设为这之外的值将不会发送失败提示，也不会丢失物品
     */
    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }
}
