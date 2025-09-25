package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.depend.mythic.IMythic;
import de.tr7zw.changeme.nbtapi.NBT;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.context.PlayerContextImpl;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import net.momirealms.customfishing.api.mechanic.item.ItemManager;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomFishingMaterial implements IMaterialAdapter {
    private final String id;

    public CustomFishingMaterial(String id) {
        this.id = id;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        BukkitCustomFishingPlugin api = BukkitCustomFishingPlugin.getInstance();
        ItemManager manager = api.getItemManager();
        return manager.buildInternal(Context.player(null).arg(ContextKeys.ID, id), id);
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return id.equals(nbt.resolveOrNull("CustomFishing.id", String.class));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomFishingMaterial)) return false;
        CustomFishingMaterial that = (CustomFishingMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
