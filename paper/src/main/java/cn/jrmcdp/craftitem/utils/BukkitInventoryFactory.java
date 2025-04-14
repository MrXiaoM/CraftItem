package cn.jrmcdp.craftitem.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

public class BukkitInventoryFactory implements InventoryFactory {
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    @Override
    public Inventory create(InventoryHolder owner, int size, String title) {
        Component parsed = AdventureUtil.miniMessage(title);
        return Bukkit.createInventory(owner, size, legacy.serialize(parsed));
    }
}
