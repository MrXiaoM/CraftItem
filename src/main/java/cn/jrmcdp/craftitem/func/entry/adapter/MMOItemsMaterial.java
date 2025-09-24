package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import de.tr7zw.changeme.nbtapi.NBT;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class MMOItemsMaterial implements IMaterialAdapter {
    private final String type;
    private final String id;

    public MMOItemsMaterial(String type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        MMOItems mi = MMOItems.plugin;
        Type type = mi.getTypes().get(this.type);
        if (type == null) return null;
        MMOItemTemplate template = mi.getTemplates().getTemplate(type, id);
        return template == null ? null : template
                .newBuilder().build()
                .newBuilder().build(true);
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return type.equals(nbt.getString("MMOITEMS_ITEM_TYPE")) && id.equals(nbt.getString("MMOITEMS_ITEM_ID"));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MMOItemsMaterial)) return false;
        MMOItemsMaterial that = (MMOItemsMaterial) o;
        return Objects.equals(type, that.type) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}
