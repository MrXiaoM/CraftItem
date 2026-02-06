package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CraftEngineMaterial implements IMaterialAdapter {
    private final String id;

    public CraftEngineMaterial(String id) {
        this.id = id;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        CustomItem<ItemStack> item = CraftEngineItems.byId(Key.of(id));
        if (item == null) return null;
        return item.item();
    }

    @Override
    public boolean match(@Nullable Player player, ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        Key key = CraftEngineItems.getCustomItemId(item);
        return key != null && id.equals(key.asString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CraftEngineMaterial)) return false;
        CraftEngineMaterial that = (CraftEngineMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
