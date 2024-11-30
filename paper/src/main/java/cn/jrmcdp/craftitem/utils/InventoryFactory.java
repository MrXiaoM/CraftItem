package cn.jrmcdp.craftitem.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public interface InventoryFactory {
    /**
     * 新建一个界面
     * @param owner holder
     * @param size 大小
     * @param title 标题，自动转换颜色字符
     */
    Inventory create(InventoryHolder owner, int size, String title);
}
