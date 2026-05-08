package cn.jrmcdp.craftitem.depend.mythic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.ItemExecutor;
import org.bukkit.inventory.ItemStack;

public class Mythic5 implements IMythic{
    MythicBukkit mythic = MythicBukkit.inst();
    @Override
    public ItemStack getItem(String mythicId) {
        return mythic.getItemManager().getItemStack(mythicId);
    }

    @Override
    public String getMythicId(ItemStack item) {
        ItemExecutor itemManager = mythic.getItemManager();
        if (itemManager.isMythicItem(item)) {
            return itemManager.getMythicTypeFromItem(item);
        } else {
            return null;
        }
    }
}
