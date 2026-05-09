package cn.jrmcdp.craftitem.depend.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.util.jnbt.CompoundTag;
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
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() <= 0) {
            return null;
        }
        CompoundTag data = mythic.getVolatileCodeHandler().getItemHandler().getNBTData(item);
        if (data != null && data.containsKey("MYTHIC_TYPE")) {
            return data.getString("MYTHIC_TYPE");
        }
        return null;
    }
}
