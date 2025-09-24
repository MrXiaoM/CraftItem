package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.inventory.ItemStack;

public interface IMaterialAdapter {
    boolean supportNewIcon();
    ItemStack getNewIcon(CraftItem plugin);
    boolean match(ItemStack item);
}
