package cn.jrmcdp.craftitem.func.entry.adapter;

import org.bukkit.inventory.ItemStack;

public class VanillaMaterial implements IMaterialAdapter {
    private final ItemStack sample;
    public VanillaMaterial(ItemStack sample) {
        this.sample = sample;
    }
    @Override
    public boolean match(ItemStack item) {
        return sample.isSimilar(item);
    }
}
