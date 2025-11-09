package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NoneAdapter implements IMaterialAdapter {
    public static final NoneAdapter INSTANCE = new NoneAdapter();
    private NoneAdapter() {}
    @Override
    public boolean supportNewIcon() {
        return false;
    }
    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean match(@Nullable Player player, ItemStack item) {
        return false;
    }
}
