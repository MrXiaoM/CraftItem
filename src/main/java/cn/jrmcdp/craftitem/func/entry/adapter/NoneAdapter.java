package cn.jrmcdp.craftitem.func.entry.adapter;

import org.bukkit.inventory.ItemStack;

public class NoneAdapter implements IMaterialAdapter {
    public static final NoneAdapter INSTANCE = new NoneAdapter();
    private NoneAdapter() {}
    @Override
    public boolean match(ItemStack item) {
        return false;
    }
}
