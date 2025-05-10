package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.func.entry.adapter.IMaterialAdapter;
import cn.jrmcdp.craftitem.func.entry.adapter.VanillaMaterial;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class MaterialAdapterManager extends AbstractModule {
    public MaterialAdapterManager(CraftItem plugin) {
        super(plugin);
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        // TODO: 支持其它插件
    }

    public List<MaterialInstance> fromMaterials(List<ItemStack> materials) {
        List<MaterialInstance> list = new ArrayList<>();
        for (ItemStack item : materials) {
            IMaterialAdapter adapter;

            // TODO: 支持其它插件

            adapter = new VanillaMaterial(item);
            list.add(new MaterialInstance(item, adapter, item.getAmount()));
        }
        return list;
    }

    public static MaterialAdapterManager inst() {
        return instanceOf(MaterialAdapterManager.class);
    }
}
