package cn.jrmcdp.craftitem.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static cn.jrmcdp.craftitem.utils.MiniMessageConvert.legacyToMiniMessage;

public class PaperInventoryFactory implements InventoryFactory {
    private final MiniMessage miniMessage;
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
        return Bukkit.createInventory(owner, size, miniMessage(title.startsWith("&") ? title : ("&0" + title)));
    }
}
