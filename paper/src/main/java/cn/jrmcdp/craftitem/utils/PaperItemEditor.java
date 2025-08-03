package cn.jrmcdp.craftitem.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;

import java.util.ArrayList;
import java.util.List;

public class PaperItemEditor implements ItemEditor {
    public PaperItemEditor() throws Exception {
        ItemMeta.class.getDeclaredMethod("displayName");
        ItemMeta.class.getDeclaredMethod("displayName", Component.class);
        ItemMeta.class.getDeclaredMethod("lore");
        ItemMeta.class.getDeclaredMethod("lore", List.class);
    }

    @Override
    public ComponentSerializer<Component, ?, String> serializer() {
        return GsonComponentSerializer.gson();
    }

    @Override
    public @Nullable Component getItemDisplayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() ? meta.displayName() : null;
    }

    @Override
    public void setItemDisplayName(ItemStack item, @Nullable Component displayName) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(displayName);
            item.setItemMeta(meta);
        }
    }

    @Override
    public @NotNull List<Component> getItemLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<Component> lore = meta.lore();
            return lore == null ? new ArrayList<>() : lore;
        }
        return new ArrayList<>();
    }

    @Override
    public void setItemLore(ItemStack item, @Nullable List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.lore(lore);
            item.setItemMeta(meta);
        }
    }
}
