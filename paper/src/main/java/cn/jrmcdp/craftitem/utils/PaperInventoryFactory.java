package cn.jrmcdp.craftitem.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

import static cn.jrmcdp.craftitem.utils.MiniMessageConvert.legacyToMiniMessage;

public class PaperInventoryFactory implements InventoryFactory {
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    public PaperInventoryFactory() {
        miniMessage = MiniMessage.builder()
                .postProcessor(it -> it.decoration(TextDecoration.ITALIC, false))
                .build();
    }

    public Component miniMessage(String text) {
        if (text == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(legacyToMiniMessage(text));
    }
    @Override
    public Inventory create(InventoryHolder owner, int size, String title) {
        try {
            Component parsed = miniMessage(title);
            return Bukkit.createInventory(owner, size, parsed);
        } catch (LinkageError e) { // 1.16 以下的旧版本 Paper 服务端不支持这个接口
            Component parsed = AdventureUtil.miniMessage(title);
            return Bukkit.createInventory(owner, size, legacy.serialize(parsed));
        }
    }

    public static boolean test() {
        try {
            Bukkit.class.getDeclaredMethod("createInventory", InventoryHolder.class, InventoryType.class, Component.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
