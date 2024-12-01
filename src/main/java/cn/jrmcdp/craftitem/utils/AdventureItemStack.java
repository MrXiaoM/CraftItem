package cn.jrmcdp.craftitem.utils;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static cn.jrmcdp.craftitem.utils.MiniMessageConvert.miniMessage;

public class AdventureItemStack {

    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        return buildItem(material, null, name, lore);
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, List<String> lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        ItemStack item = new ItemStack(material, 1);
        if (name != null) setItemDisplayName(item, name);
        if (lore != null) setItemLore(item, lore);
        if (customModelData != null) setCustomModelData(item, customModelData);
        return item;
    }

    public static void setItemDisplayName(ItemStack item, String name) {
        if (item == null) return;
        Component displayName = miniMessage(name);
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            String json = GsonComponentSerializer.gson().serialize(displayName);
            setItemDisplayNameByJson(item, json);
        } else {
            String legacy = LegacyComponentSerializer.legacySection().serialize(displayName);
            setItemDisplayNameByJson(item, legacy);
        }
    }

    public static void setItemDisplayNameByJson(ItemStack item, String json) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                nbt.setString("minecraft:custom_name", json);
            });
        } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                display.setString("Name", json);
            });
        }
    }

    public static void setItemLore(ItemStack item, List<String> lore) {
        if (item == null) return;
        List<String> json = new ArrayList<>();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) {
            for (String s : lore) {
                Component line = miniMessage(s);
                json.add(GsonComponentSerializer.gson().serialize(line));
            }
        } else {
            for (String s : lore) {
                Component line = miniMessage(s);
                json.add(LegacyComponentSerializer.legacySection().serialize(line));
            }
        }
        setItemLoreByJson(item, json);
    }

    public static void setItemLoreByJson(ItemStack item, List<String> json) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                ReadWriteNBTList<String> list = nbt.getStringList("minecraft:lore");
                if (!list.isEmpty()) list.clear();
                list.addAll(json);
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                ReadWriteNBTList<String> list = display.getStringList("Lore");
                if (!list.isEmpty()) list.clear();
                list.addAll(json);
            });
        }
    }

    @Nullable
    public static List<String> getItemLoreAsMiniMessage(ItemStack item) {
        List<Component> components = getItemLore(item);
        if (components == null) return null;
        List<String> lore = new ArrayList<>();
        for (Component component : components) {
            String s = miniMessage(component);
            lore.add(s);
        }
        return lore;
    }

    @Nullable
    public static List<Component> getItemLore(ItemStack item) {
        List<String> loreAsJson = getItemLoreAsJson(item);
        if (loreAsJson == null) return null;
        List<Component> lore = new ArrayList<>();
        for (String line : loreAsJson) {
            Component component = GsonComponentSerializer.gson().deserialize(line);
            lore.add(component);
        }
        return lore;
    }

    @Nullable
    public static List<String> getItemLoreAsJson(ItemStack item) {
        if (item == null) return null;
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            return nbt != null && nbt.hasTag("minecraft:custom_name", NBTType.NBTTagList)
                    ? nbt.getStringList("minecraft:lore").toListCopy()
                    : null;
        } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                return display != null && display.hasTag("Lore", NBTType.NBTTagList)
                        ? display.getStringList("Lore").toListCopy()
                        : null;
            });
        } else {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                if (display == null || !display.hasTag("Lore", NBTType.NBTTagList)) return null;
                List<String> lore = new ArrayList<>();
                for (ReadWriteNBT compound : display.getCompoundList("Lore")) {
                    lore.add(compound.toString());
                }
                return lore;
            });
        }
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
        }
    }
}
