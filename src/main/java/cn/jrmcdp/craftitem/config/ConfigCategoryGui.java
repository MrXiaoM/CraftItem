package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Icon;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.func.AbstractModule;
import cn.jrmcdp.craftitem.gui.GuiCategory;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ConfigUpdater;

import java.util.*;

@AutoRegister
public class ConfigCategoryGui extends AbstractModule {
    private String title;
    private String[] chest;
    private Map<String, Icon> items;
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
            String rawMaterial = section.getString(key + ".Type", "STONE");
            Material material = Utils
                    .parseMaterial(rawMaterial)
                    .orElse(null);
            if (material == null) {
                material = Material.STONE;
                CraftItem.getPlugin().getLogger().warning("Category.yml 找不到图标 " + key + " 设定的物品类型 " + rawMaterial + "，使用默认图标");
            }
            items.put(key, new Icon(material, key, section));
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
