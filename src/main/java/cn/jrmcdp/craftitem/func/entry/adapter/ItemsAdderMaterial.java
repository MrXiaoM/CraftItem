package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemsAdderMaterial implements IMaterialAdapter {
    private final String namespace;
    private final String id;
    public ItemsAdderMaterial(String namespace, String id) {
        this.namespace = namespace;
        this.id = id;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        CustomStack instance = CustomStack.getInstance(namespace + ":" + id);
        return instance == null ? null : instance.getItemStack();
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            ReadableNBT itemsadder = nbt.getCompound("itemsadder");
            return itemsadder != null
                    && namespace.equals(itemsadder.getString("namespace"))
                    && id.equals(itemsadder.getString("id"));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemsAdderMaterial)) return false;
        ItemsAdderMaterial that = (ItemsAdderMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
