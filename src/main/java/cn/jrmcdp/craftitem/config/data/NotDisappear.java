package cn.jrmcdp.craftitem.config.data;

import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.utils.Utils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NotDisappear {
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private final List<Material> notDisappearMaterials = new ArrayList<>();
    private final List<String> notDisappearNames = new ArrayList<>();
    private final List<String> notDisappearLores = new ArrayList<>();
    private final Map<String, List<String>> notDisappearNBTStrings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public void reloadConfig(MemoryConfiguration config) {
        notDisappearMaterials.clear();
        notDisappearNames.clear();
        notDisappearLores.clear();
        notDisappearNBTStrings.clear();
        for (String s : config.getStringList("DoNotDisappear.Material")) {
            Material material = Utils.parseMaterial(s).orElse(null);
            if (material == null) continue;
            notDisappearMaterials.add(material);
        }
        for (String s : config.getStringList("DoNotDisappear.Name")) {
            Component component = AdventureUtil.miniMessage(s);
            notDisappearNames.add(legacy.serialize(component));
        }
        for (String s : config.getStringList("DoNotDisappear.Lore")) {
            Component component = AdventureUtil.miniMessage(s);
            notDisappearLores.add(legacy.serialize(component));
        }
        ConfigurationSection section = config.getConfigurationSection("DoNotDisappear.NBTString");
        if (section != null) for (String key : section.getKeys(false)) {
            List<String> list = section.getStringList(key);
            notDisappearNBTStrings.put(key, list);
        }
    }

    public List<MaterialInstance> filterMaterials(List<MaterialInstance> materials) {
        List<MaterialInstance> list = new ArrayList<>();
        if (materials.isEmpty()) return list;
        for (MaterialInstance material : materials) {
            if (isNotDisappearItem(material.getSample())) continue;
            list.add(material);
        }
        return list;
    }

    public boolean isNotDisappearItem(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return true;
        if (notDisappearMaterials.contains(item.getType())) return true;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (!notDisappearNames.isEmpty()) {
                String displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
                if (displayName != null && !displayName.isEmpty()) {
                    for (String s : notDisappearNames) {
                        if (displayName.contains(s)) return true;
                    }
                }
            }
            if (!notDisappearLores.isEmpty()) {
                List<String> lore = meta.hasLore() ? meta.getLore() : null;
                if (lore != null && !lore.isEmpty()) {
                    String loreStr = String.join("\n", lore);
                    for (String s : notDisappearLores) {
                        if (loreStr.contains(s)) return true;
                    }
                }
            }
        }
        if (!notDisappearNBTStrings.isEmpty()) {
            return NBT.get(item, nbt -> {
                for (Map.Entry<String, List<String>> entry : notDisappearNBTStrings.entrySet()) {
                    if (nbt.hasTag(entry.getKey(), NBTType.NBTTagString)) {
                        List<String> list = entry.getValue();
                        if (list.isEmpty()) return true;
                        String value = nbt.getString(entry.getKey());
                        for (String s : list) {
                            if (value.contains(s)) return true;
                        }
                    }
                }
                return false;
            });
        }
        return false;
    }
}
