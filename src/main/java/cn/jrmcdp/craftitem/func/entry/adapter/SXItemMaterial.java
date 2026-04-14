package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import github.saukiya.sxitem.SXItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SXItemMaterial implements IMaterialAdapter {
    private final String itemKey;

    public SXItemMaterial(String itemKey) {
        this.itemKey = itemKey;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public ItemStack getNewIcon(CraftItem plugin) {
        ItemStack item = SXItem.getItemManager().getItem(itemKey);
        if (item == null || item.getType().equals(Material.AIR)) {
            return null;
        }
        return item;
    }

    @Override
    public boolean match(@Nullable Player player, ItemStack item) {
        return itemKey.equals(SXItem.getItemManager().getItemKey(item));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SXItemMaterial)) return false;
        SXItemMaterial that = (SXItemMaterial) o;
        return Objects.equals(itemKey, that.itemKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemKey);
    }
}
