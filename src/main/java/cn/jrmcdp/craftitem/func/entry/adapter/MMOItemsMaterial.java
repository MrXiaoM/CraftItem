package cn.jrmcdp.craftitem.func.entry.adapter;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MMOItemsMaterial implements IMaterialAdapter {
    private final String type;
    private final String id;

    public MMOItemsMaterial(String type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return type.equals(nbt.getString("MMOITEMS_ITEM_TYPE")) && id.equals(nbt.getString("MMOITEMS_ITEM_ID"));
        });
    }
}
