package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("RedundantCast") // jdk1.8没有记录类需要强转Object调用方法
public class CraftEngineMaterial implements IMaterialAdapter {
    private final Key itemId;
    @Nullable
    private Integer hashCode;

    public CraftEngineMaterial(Key itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        CustomItem<ItemStack> item = CraftEngineItems.byId(this.itemId);
        if (item == null) return null;
        return item.buildItemStack();
    }

    @Override
    public boolean match(@Nullable Player player, ItemStack item) {
        return ((Object) this.itemId).equals(CraftEngineItems.getCustomItemId(item));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CraftEngineMaterial)) return false;
        return ((Object) this.itemId).equals(((CraftEngineMaterial) o).itemId);
    }

    @Override
    public int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = ((Object) this.itemId).toString().hashCode();
        }
        return this.hashCode;
    }
}
