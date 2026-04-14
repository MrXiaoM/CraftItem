package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.func.entry.adapter.*;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import github.saukiya.sxitem.SXItem;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pers.neige.neigeitems.manager.ItemManager;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;

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
    boolean enableSXItem;
    boolean enableNeigeItems;
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
        if (Util.isPresent("net.momirealms.craftengine.bukkit.api.CraftEngineItems")) {
            enableCraftEngine = config.getBoolean("Material-Adapters.CraftEngine.enable", true);
        } else {
            enableCraftEngine = false;
        }
        if (Util.isPresent("github.saukiya.sxitem.SXItem")) {
            enableSXItem = config.getBoolean("Material-Adapters.SX-Item.enable", true);
        } else {
            enableSXItem = false;
        }
        if (Util.isPresent("pers.neige.neigeitems.manager.ItemManager")) {
            enableNeigeItems = config.getBoolean("Material-Adapters.NeigeItems.enable", true);
        } else {
            enableNeigeItems = false;
        }
    }

    public List<MaterialInstance> fromMaterials(List<@NotNull ItemStack> materials) {
        List<MaterialInstance> list = new ArrayList<>();
        for (ItemStack item : materials) {
            IMaterialAdapter adapter = null;
            if (!item.getType().equals(Material.AIR) && item.getAmount() > 0) {
                // 先判定外置的材料适配器
                for (Function<ItemStack, IMaterialAdapter> func : externalAdapters.values()) {
                    adapter = func.apply(item);
                    if (adapter != null) {
                        break;
                    }
                }
                // 再判定内置的材料适配器
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
                    Key itemId = CraftEngineItems.getCustomItemId(item);
                    if (itemId != null) {
                        adapter = new CraftEngineMaterial(itemId);
                    }
                }
                if (enableSXItem && adapter == null) {
                    String itemKey = SXItem.getItemManager().getItemKey(item);
                    if (itemKey != null) {
                        adapter = new SXItemMaterial(itemKey);
                    }
                }
                if (enableNeigeItems && adapter == null) {
                    String itemId = ItemManager.INSTANCE.getItemId(item);
                    if (itemId != null) {
                        adapter = new NeigeItemsMaterial(itemId);
                    }
                }
                if (adapter == null) {
                    adapter = new VanillaMaterial(item);
                }
            }
            if (adapter == null) {
                adapter = NoneAdapter.INSTANCE;
            }
            if (useNewIcon && adapter.supportNewIcon()) {
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
