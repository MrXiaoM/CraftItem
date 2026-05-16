package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.item.processor.ItemNameProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;

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
        BukkitItemDefinition item = CraftEngineItems.byId(this.itemId);
        if (item == null) return null;
        return item.buildBukkitItem();
    }

    @Override
    public boolean match(@Nullable Player player, ItemStack item) {
        // noinspection RedundantCast
        return ((Object) this.itemId).equals(CraftEngineItems.getCustomItemId(item));
    }

    @Nullable
    public static String getTranslationKey(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() < 1) {
            return null;
        }
        BukkitItemDefinition customItem = CraftEngineItems.byItemStack(item);
        if (customItem == null) {
            return null;
        }
        return customItem.translationKey();
    }

    public static String getItemName(ItemStack item) {
        BukkitItemDefinition customItem = CraftEngineItems.byItemStack(item);
        if (customItem != null) {
            String displayName = AdventureItemStack.getItemDisplayNameAsMiniMessage(item);
            if (displayName != null) {
                return displayName.replace("&", "&&");
            }
            // 如果还有通过物品处理器添加的名字，优先返回
            for (ItemProcessor processor : customItem.processors()) {
                if (processor instanceof ItemNameProcessor) {
                    return ((ItemNameProcessor) processor).itemName();
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CraftEngineMaterial)) return false;
        // noinspection RedundantCast
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
