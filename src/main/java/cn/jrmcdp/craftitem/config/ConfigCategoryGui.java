package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.func.AbstractModule;
import cn.jrmcdp.craftitem.gui.GuiCategory;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.ConfigUpdater;

import java.util.*;

@AutoRegister
public class ConfigCategoryGui extends AbstractModule {
    private String title;
    private String[] chest;
    private Map<String, ItemStack> items;
    private int slotAmount;
    private final ConfigUpdater updater;
    public ConfigCategoryGui(CraftItem plugin) {
        super(plugin);
        updater = ConfigUpdater.create(plugin, "Category.yml");
        updater.fullMatch("Title")
                .fullMatch("Chest")
                .fullMatch("Item.上")
                .fullMatch("Item.下");
    }

    public int getSlotAmount() {
        return slotAmount;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        YamlConfiguration config = ConfigUtils.loadPluginConfig(plugin, "Category.yml");
        if (plugin.isEnableConfigUpdater()) {
            updater.apply(config, plugin.resolve("./Category.yml"));
        }
        title = config.getString("Title");
        chest = new String[config.getStringList("Chest").size() * 9];
        StringBuilder info = new StringBuilder();
        for (String line : config.getStringList("Chest")) {
            info.append(line);
        }
        chest = info.toString().split("");
        slotAmount = 0;
        for (String key : chest) {
            if (key.equals("方")) {
                slotAmount++;
            }
        }
        items = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("Item");
        if (section != null) for (String key : section.getKeys(false)) {
            ItemStack itemStack = Utils
                    .parseMaterial(section.getString(key + ".Type", "STONE"))
                    .map(ItemStack::new)
                    .orElseGet(() -> new ItemStack(Material.STONE));
            if (!itemStack.getType().equals(Material.AIR)) {
                String name = section.getString(key + ".Name");
                List<String> lore = section.getStringList(key + ".Lore");
                if (name != null) AdventureItemStack.setItemDisplayName(itemStack, name);
                if (!lore.isEmpty()) AdventureItemStack.setItemLoreMiniMessage(itemStack, lore);
            }
            items.put(key, itemStack);
        }
    }

    public void openGui(PlayerData playerData, String type, List<String> craftList, int page) {
        plugin.getScheduler().runTask(() -> {
            new GuiCategory(this, title, chest, items, slotAmount, playerData, type, craftList, page).open();
        });
    }

    public static ConfigCategoryGui inst() {
        return instanceOf(ConfigCategoryGui.class);
    }
}
