package cn.jrmcdp.craftitem.depend.mythic;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.inventory.ItemStack;

public class Mythic5 implements IMythic{
    MythicBukkit mythic = MythicBukkit.inst();
    @Override
    public ItemStack getItem(String mythicId) {
        return mythic.getItemManager().getItemStack(mythicId);
    }
}
