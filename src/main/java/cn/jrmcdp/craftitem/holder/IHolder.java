package cn.jrmcdp.craftitem.holder;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface IHolder extends IAutoCloseHolder {
    void onClick(InventoryClickEvent event);

    default void onSecond() {

    }
}
