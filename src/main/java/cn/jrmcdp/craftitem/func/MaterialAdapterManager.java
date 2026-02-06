package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.func.entry.adapter.*;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AutoRegister
public class MaterialAdapterManager extends AbstractModule {
    boolean useNewIcon;
    boolean enableMMOItems;
    boolean enableMythicMobs;
    boolean enableItemsAdder;
    boolean enableCustomFishing;
    boolean enableCraftEngine;
    private final Map<String, Function<ItemStack, IMaterialAdapter>> externalAdapters = new HashMap<>();
    public MaterialAdapterManager(CraftItem plugin) {
        super(plugin);
    }

    public void register(String id, Function<ItemStack, IMaterialAdapter> func) {
        externalAdapters.put(id, func);
    }

    public void unregister(String id) {
        externalAdapters.remove(id);
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        useNewIcon = config.getBoolean("Material-Adapters.Use-New-Icon", false);
        enableMMOItems = config.getBoolean("Material-Adapters.MMOItems.enable", true);
        enableMythicMobs = config.getBoolean("Material-Adapters.MythicMobs.enable", true);
        enableItemsAdder = config.getBoolean("Material-Adapters.ItemsAdder.enable", true);
        enableCustomFishing = config.getBoolean("Material-Adapters.CustomFishing.enable", true);
        enableCraftEngine = config.getBoolean("Material-Adapters.CraftEngine.enable", true);
    }

    public List<MaterialInstance> fromMaterials(List<ItemStack> materials) {
        List<MaterialInstance> list = new ArrayList<>();
        for (ItemStack item : materials) {
            IMaterialAdapter adapter = null;
            if (item != null && !item.getType().equals(Material.AIR)) {
                if (adapter == null) {
                    for (Function<ItemStack, IMaterialAdapter> func : externalAdapters.values()) {
                        adapter = func.apply(item);
                        if (adapter != null) {
                            break;
                        }
                    }
                }
                if (enableMMOItems && adapter == null) {
                    adapter = NBT.get(item, nbt -> {
                        String type = nbt.getString("MMOITEMS_ITEM_TYPE");
                        String id = nbt.getString("MMOITEMS_ITEM_ID");
                        if (type != null && !type.isEmpty() && id != null && !id.isEmpty()) {
                            return new MMOItemsMaterial(type, id);
                        }
                        return null;
                    });
                }
                if (enableMythicMobs && adapter == null) {
                    adapter = NBT.get(item, nbt -> {
                        String mythicId = nbt.getString("MYTHIC_TYPE");
                        if (mythicId != null && !mythicId.isEmpty()) {
                            return new MythicMobsMaterial(mythicId);
                        }
                        return null;
                    });
                }
                if (enableItemsAdder && adapter == null) {
                    adapter = NBT.get(item, nbt -> {
                        ReadableNBT itemsadder = nbt.getCompound("itemsadder");
                        if (itemsadder == null) return null;
                        String namespace = itemsadder.getString("namespace");
                        String id = itemsadder.getString("id");
                        if (namespace != null && !namespace.isEmpty() && id != null && !id.isEmpty()) {
                            return new ItemsAdderMaterial(namespace, id);
                        }
                        return null;
                    });
                }
                if (enableCustomFishing && adapter == null) {
                    adapter = NBT.get(item, nbt -> {
                        String id = nbt.resolveOrNull("CustomFishing.id", String.class);
                        if (id != null && !id.isEmpty()) {
                            return new CustomFishingMaterial(id);
                        }
                        return null;
                    });
                }
                if (enableCraftEngine && adapter == null) {
                    if (CraftEngineItems.isCustomItem(item)) {
                        Object key = CraftEngineItems.getCustomItemId(item);
                        if (key != null) {
                            adapter = new CraftEngineMaterial(key);
                        }
                    }
                }
                if (adapter == null) {
                    adapter = new VanillaMaterial(item);
                }
            }
            if (adapter == null) {
                adapter = NoneAdapter.INSTANCE;
            }
            if (adapter != null && useNewIcon && adapter.supportNewIcon()) {
                ItemStack sample = adapter.getNewIcon(plugin);
                if (sample != null) {
                    sample.setAmount(item.getAmount());
                } else {
                    sample = item;
                }
                list.add(new MaterialInstance(sample.clone(), adapter, sample.getAmount()));
            } else {
                list.add(new MaterialInstance(item.clone(), adapter, item.getAmount()));
            }
        }
        return list;
    }

    public static MaterialAdapterManager inst() {
        return instanceOf(MaterialAdapterManager.class);
    }
}
