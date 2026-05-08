package cn.jrmcdp.craftitem.depend.mythic;

import de.tr7zw.changeme.nbtapi.NBT;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Mythic4 implements IMythic {
    MythicMobs mythic = MythicMobs.inst();
    @Override
    public ItemStack getItem(String mythicId) {
        return mythic.getItemManager().getItemStack(mythicId);
    }

    @Override
    public String getMythicId(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return null;
        return NBT.get(item, nbt -> {
            if (nbt.hasTag("MYTHIC_TYPE")) {
                return nbt.getString("MYTHIC_TYPE");
            } else {
                return null;
            }
        });
    }
}
