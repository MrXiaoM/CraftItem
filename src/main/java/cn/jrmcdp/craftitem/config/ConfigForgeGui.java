package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Icon;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.func.AbstractModule;
import cn.jrmcdp.craftitem.gui.GuiForge;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import cn.jrmcdp.craftitem.utils.Utils;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ColorHelper;

import java.util.*;

@AutoRegister
public class ConfigForgeGui extends AbstractModule {
    private String title;
    private char[] chest;
    private char[] chestTime;
    public final Map<String, Icon> items = new HashMap<>();
    public ConfigForgeGui(CraftItem plugin) {
        super(plugin);
    }

    public char[] getChest() {
        return chest;
    }

    public char[] getChestTime() {
        return chestTime;
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        YamlConfiguration config = ConfigUtils.loadPluginConfig(plugin, "Gui.yml");
        title = ColorHelper.parseColor(config.getString("Title"));

        chest = String.join("", config.getStringList("Chest")).toCharArray();
        chestTime = String.join("", config.getStringList("ChestTime")).toCharArray();

        items.clear();
        List<String> necessaryItems = Lists.newArrayList(
                "材", "物", "锻", "锻_连击", "锻_困难",
                "时", "时_未开启", "时_条件不足", "时_进行中", "时_完成"
        );
        ConfigurationSection section = config.getConfigurationSection("Item");
        if (section != null) for (String key : section.getKeys(false)) {
            String rawMaterial = section.getString(key + ".Type", "STONE");
            Material material = Utils
                    .parseMaterial(rawMaterial)
                    .orElse(null);
            if (material == null) {
                if (necessaryItems.contains(key)) {
                    material = Material.STONE;
                    CraftItem.getPlugin().getLogger().warning("Gui.yml 找不到图标 " + key + " 设定的物品类型 " + rawMaterial + "，使用默认图标");
                } else {
                    CraftItem.getPlugin().getLogger().warning("Gui.yml 找不到图标 " + key + " 设定的物品类型 " + rawMaterial);
                    continue;
                }
            }

            String name = ColorHelper.parseColor(section.getString(key + ".Name"));
            int data = section.getInt(key + ".Data", 0);
            int amount = section.getInt(key + ".Amount", 1);
            List<String> lore = ColorHelper.parseColor(section.getStringList(key + ".Lore"));
            Integer customModelData = section.contains(key + ".CustomModelData") ? section.getInt(key + ".CustomModelData") : null;
            List<String> leftClick = ColorHelper.parseColor(section.getStringList(key + ".LeftClick"));
            List<String> rightClick = ColorHelper.parseColor(section.getStringList(key + ".RightClick"));
            List<String> shiftLeftClick = ColorHelper.parseColor(section.getStringList(key + ".ShiftLeftClick"));
            List<String> shiftRightClick = ColorHelper.parseColor(section.getStringList(key + ".ShiftRightClick"));
            items.put(key, new Icon(material, data, amount, name, lore, customModelData, leftClick, rightClick, shiftLeftClick, shiftRightClick));
        }

        checkError(chest, "Chest");
        checkError(chestTime, "ChestTime");
    }

    private void checkError(char[] inv, String name) {
        if (inv.length % 9 != 0) {
            warn("Gui.yml 配置有误: " + name + " 的长度 (" + inv.length + ") 不为 9 的倍数。如果你没有修改过这个选项，这可能由编码错误引起。");
            return;
        }
        for (int i = 0; i < inv.length; i++) {
            char c = inv[i];
            String key = String.valueOf(c);
            if (!items.containsKey(key) && !key.equals(" ") && !key.equals("　")) {
                inv[i] = '　';
                warn("Gui.yml 配置有误: 无法找到 " + name + " 指定的图标 '" + key + "'");
            }
        }
    }

    public void openGui(PlayerData playerData, String id, CraftData craftData) {
        plugin.getScheduler().runTask(() -> {
            new GuiForge(this, title, items, playerData, id, craftData).open();
        });
    }

    public GuiForge buildGui(PlayerData playerData, String id, CraftData craftData) {
        return new GuiForge(this, title, items, playerData, id, craftData);
    }

    public static ConfigForgeGui inst() {
        return instanceOf(ConfigForgeGui.class);
    }
}
