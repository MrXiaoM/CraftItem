package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import pers.neige.neigeitems.manager.ItemManager;

import java.util.Objects;

public class NeigeItemsMaterial implements IMaterialAdapter {
    private final String itemId;

    public NeigeItemsMaterial(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        ItemStack item = ItemManager.INSTANCE.getItemStack(itemId);
        if (item == null || item.getType().equals(Material.AIR)) {
            return null;
        }
        return item;
    }

    @Override
    public boolean match(@Nullable Player player, ItemStack item) {
        return itemId.equals(ItemManager.INSTANCE.getItemId(item));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NeigeItemsMaterial)) return false;
        NeigeItemsMaterial that = (NeigeItemsMaterial) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId);
    }
}
