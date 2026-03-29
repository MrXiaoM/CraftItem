package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.processor.ItemNameProcessor;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;

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

    @Nullable
    public static String getTranslationKey(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() < 1) {
            return null;
        }
        CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(item);
        if (customItem == null || customItem.isEmpty()) {
            return null;
        }
        return customItem.translationKey();
    }

    public static String getItemName(ItemStack item) {
        CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(item);
        if (customItem != null && !customItem.isEmpty()) {
            String displayName = AdventureItemStack.getItemDisplayNameAsMiniMessage(item);
            if (displayName != null) {
                return displayName.replace("&", "&&");
            }
            // 如果还有通过物品处理器添加的名字，优先返回
            for (ItemProcessor processor : customItem.dataModifiers()) {
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
