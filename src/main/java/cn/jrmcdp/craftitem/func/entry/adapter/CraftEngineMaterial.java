package cn.jrmcdp.craftitem.func.entry.adapter;

import cn.jrmcdp.craftitem.CraftItem;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Objects;

public class CraftEngineMaterial implements IMaterialAdapter {
    private final Object key;
    private String id;
    private static Class<?> KEY_CLASS;
    private static Method AS_STRING_METHOD;
    private static Method EQUALS_METHOD;

    static {
        try {
            KEY_CLASS = Class.forName("net.momirealms.craftengine.core.util.Key");
            AS_STRING_METHOD = KEY_CLASS.getDeclaredMethod("asString");
            EQUALS_METHOD = KEY_CLASS.getDeclaredMethod("equals", Object.class);
        } catch (Exception e) {
            KEY_CLASS = null;
            CraftItem.getPlugin().getLogger().warning("CraftEngine not available: " + e.getMessage());
        }
    }

    public CraftEngineMaterial(Object key) {
        this.key = key;
        id = getId(key);
    }

    @Override
    public boolean supportNewIcon() {
        return true;
    }

    @Override
    public ItemStack getNewIcon(CraftItem plugin) {
        CustomItem<ItemStack> item = CraftEngineItems.byId((Key) key);
        if (item == null) return null;
        return item.buildItemStack();
    }

    @Override
    public boolean match(@Nullable Player player, ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;

        try {
            Object that = CraftEngineItems.getCustomItemId(item);
            if (that == null) return false;
            return (boolean) EQUALS_METHOD.invoke(this.key, that);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CraftEngineMaterial)) return false;
        CraftEngineMaterial that = (CraftEngineMaterial) o;

        try {
            return (boolean) EQUALS_METHOD.invoke(this.key, that.key);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private String getId(Object key) {
        try {
            return (String) AS_STRING_METHOD.invoke(key);
        } catch (Exception e) {
            return null;
        }
    }
}