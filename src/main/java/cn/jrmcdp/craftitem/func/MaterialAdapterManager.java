package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.func.entry.adapter.IMaterialAdapter;
import cn.jrmcdp.craftitem.func.entry.adapter.MMOItemsMaterial;
import cn.jrmcdp.craftitem.func.entry.adapter.NoneAdapter;
import cn.jrmcdp.craftitem.func.entry.adapter.VanillaMaterial;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@AutoRegister
public class MaterialAdapterManager extends AbstractModule {
    boolean enableMMOItems;
    public MaterialAdapterManager(CraftItem plugin) {
        super(plugin);
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        enableMMOItems = config.getBoolean("Material-Adapters.MMOItems.enable", false);
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
                if (adapter == null) {
                    adapter = new VanillaMaterial(item);
                }
            }
            if (adapter == null) {
                adapter = NoneAdapter.INSTANCE;
            }
            list.add(new MaterialInstance(item, adapter, item.getAmount()));
        }
        return list;
    }

    public static MaterialAdapterManager inst() {
        return instanceOf(MaterialAdapterManager.class);
    }
}
