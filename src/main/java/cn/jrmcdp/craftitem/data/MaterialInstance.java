package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.func.entry.adapter.IMaterialAdapter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MaterialInstance {
    private final ItemStack sample;
    private final IMaterialAdapter adapter;
    protected int amount;

    public MaterialInstance(ItemStack sample, IMaterialAdapter adapter, int amount) {
        this.sample = sample;
        this.adapter = adapter;
        this.amount = amount;
    }

    public ItemStack getSample() {
        return sample;
    }

    public IMaterialAdapter getAdapter() {
        return adapter;
    }

    public int getAmount() {
        return amount;
    }

    public MaterialInstance clone() {
        return new MaterialInstance(sample, adapter, amount);
    }

    public Mutable toMutable() {
        return new Mutable(sample, adapter, amount);
    }

    public static List<Mutable> toMutable(List<MaterialInstance> materials) {
        List<Mutable> list = new ArrayList<>();
        for (MaterialInstance material : materials) {
            list.add(material.toMutable());
        }
        return list;
    }

    public static class Mutable extends MaterialInstance {
        public Mutable(ItemStack sample, IMaterialAdapter adapter, int amount) {
            super(sample, adapter, amount);
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
