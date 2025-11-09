package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IMaterialAdapter {
    boolean supportNewIcon();
    ItemStack getNewIcon(CraftItem plugin);
    boolean match(@Nullable Player player, ItemStack item);
}
