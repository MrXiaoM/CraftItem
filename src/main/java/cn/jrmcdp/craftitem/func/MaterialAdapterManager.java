package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.func.entry.adapter.*;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class MaterialAdapterManager extends AbstractModule {
    boolean useNewIcon;
    boolean enableMMOItems;
    boolean enableMythicMobs;
    boolean enableItemsAdder;
    boolean enableCustomFishing;
    public MaterialAdapterManager(CraftItem plugin) {
        super(plugin);
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        useNewIcon = config.getBoolean("Material-Adapters.Use-New-Icon", false);
        enableMMOItems = config.getBoolean("Material-Adapters.MMOItems.enable", false);
        enableMythicMobs = config.getBoolean("Material-Adapters.MythicMobs.enable", false);
        enableItemsAdder = config.getBoolean("Material-Adapters.ItemsAdder.enable", false);
        enableCustomFishing = config.getBoolean("Material-Adapters.CustomFishing.enable", false);
    }

    public List<MaterialInstance> fromMaterials(List<ItemStack> materials) {
        List<MaterialInstance> list = new ArrayList<>();
        for (ItemStack item : materials) {
            IMaterialAdapter adapter = null;
            if (item != null && !item.getType().equals(Material.AIR)) {
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
                        String mythicId = nbt.getString("MYTHIC_ITEM");
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
