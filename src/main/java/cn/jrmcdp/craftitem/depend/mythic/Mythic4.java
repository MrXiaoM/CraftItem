package cn.jrmcdp.craftitem.depend.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.inventory.ItemStack;

public class Mythic4 implements IMythic {
    MythicMobs mythic = MythicMobs.inst();
    @Override
    public ItemStack getItem(String mythicId) {
        return mythic.getItemManager().getItemStack(mythicId);
    }
}
