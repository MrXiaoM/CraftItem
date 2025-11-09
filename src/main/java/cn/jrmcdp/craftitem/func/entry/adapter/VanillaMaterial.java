package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VanillaMaterial implements IMaterialAdapter {
    private final ItemStack sample;
    public VanillaMaterial(ItemStack sample) {
        this.sample = sample.clone();
        this.sample.setAmount(1);
    }
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
        return sample.isSimilar(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VanillaMaterial)) return false;
        VanillaMaterial that = (VanillaMaterial) o;
        return Objects.equals(sample, that.sample);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sample);
    }
}
