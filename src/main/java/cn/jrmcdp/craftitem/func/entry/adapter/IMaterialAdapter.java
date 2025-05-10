package cn.jrmcdp.craftitem.func.entry.adapter;

import org.bukkit.inventory.ItemStack;

public interface IMaterialAdapter {
    boolean match(ItemStack item);
}
