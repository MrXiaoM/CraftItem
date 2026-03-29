package cn.jrmcdp.craftitem.config.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.ColorHelper;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

public class Icon {
    public final Material material;
    public final String redirect;
    public final int data;
    public final int amount;
    public final String name;
    public final List<String> lore;
    public final Integer customModelData;
    public final List<IAction> leftClick;
    public final List<IAction> rightClick;
    public final List<IAction> shiftLeftClick;
    public final List<IAction> shiftRightClick;

    public Icon(Material material, String key, ConfigurationSection section) {
        this.material = material;

        this.redirect = section.getString(key + ".Redirect", "");
        this.name = ColorHelper.parseColor(section.getString(key + ".Name", ""));
        this.data = section.getInt(key + ".Data", 0);
        this.amount = section.getInt(key + ".Amount", 1);
        this.lore = ColorHelper.parseColor(section.getStringList(key + ".Lore"));
        this.customModelData = section.contains(key + ".CustomModelData") ? section.getInt(key + ".CustomModelData") : null;
        this.leftClick = loadActions(section, key + ".LeftClick");
        this.rightClick = loadActions(section, key + ".RightClick");
        this.shiftLeftClick = loadActions(section, key + ".ShiftLeftClick");
        this.shiftRightClick = loadActions(section, key + ".ShiftRightClick");
    }

    @SafeVarargs
    public final ItemStack getItem(Player player, Pair<String, Object>... replacements) {
        ItemStack item = data > 0 ? new ItemStack(material, amount, (short) data) : new ItemStack(material, amount);
        if (item.getType().equals(Material.AIR)) return item;
        if (name != null) AdventureItemStack.setItemDisplayName(item, name);
        if (!lore.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String s : PAPI.setPlaceholders(player, this.lore)) {
                for (Pair<String, Object> pair : replacements) {
                    if (s.contains(pair.getKey())) {
                        s = s.replace(pair.getKey(), pair.getValue().toString());
                    }
                }
                lore.add(s);
            }
            AdventureItemStack.setItemLoreMiniMessage(item, lore);
        }
        if (customModelData != null) AdventureItemStack.setCustomModelData(item, customModelData);
        return item;
    }

    public void leftClick(Player player) {
        runCommands(player, leftClick);
    }

    public void rightClick(Player player) {
        runCommands(player, rightClick);
    }

    public void shiftLeftClick(Player player) {
        runCommands(player, shiftLeftClick);
    }

    public void shiftRightClick(Player player) {
        runCommands(player, shiftRightClick);
    }

    public static void runCommands(Player player, List<IAction> commands) {
        if (commands == null || commands.isEmpty()) return;
        for (IAction action : commands) {
            action.run(player);
        }
    }
}
