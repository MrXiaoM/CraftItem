package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.depend.mythic.IMythic;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MythicMobsMaterial implements IMaterialAdapter {
    private final String id;

    public MythicMobsMaterial(String id) {
        this.id = id;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        IMythic mythic = plugin.getMythic();
        if (mythic != null) {
            return mythic.getItem(id);
        }
        return null;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return id.equals(nbt.getString("MYTHIC_ITEM"));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MythicMobsMaterial)) return false;
        MythicMobsMaterial that = (MythicMobsMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
