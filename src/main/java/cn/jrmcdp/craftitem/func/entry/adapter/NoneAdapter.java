package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.inventory.ItemStack;

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
    public boolean match(ItemStack item) {
        return false;
    }
}
