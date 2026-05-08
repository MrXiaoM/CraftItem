package cn.jrmcdp.craftitem.actions;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigCategoryGui;
import cn.jrmcdp.craftitem.gui.GuiForge;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ActionBack implements IAction {
    public static final IActionProvider PROVIDER;
    public static final ActionBack INSTANCE;
    static {
        INSTANCE = new ActionBack();
        PROVIDER = input -> {
            if (input instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) input;
                if ("back".equals(section.getString("type"))) {
                    return INSTANCE;
                }
            } else {
                String s = String.valueOf(input);
                if (s.equals("[back]") || s.equals("back")) {
                    return INSTANCE;
                }
            }
            return null;
        };
    }
    private ActionBack() {
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        if (player == null) return;
        Inventory inv = player.getOpenInventory().getTopInventory();
        InventoryHolder holder = Utils.getHolder(inv);
        if (holder instanceof GuiForge) {
            GuiForge gui = (GuiForge) holder;
            String category = gui.getCategory();
            if (category != null) {
                List<String> list = CraftItem.getPlugin().config().getCategory().get(category);
                if (list != null) {
                    ConfigCategoryGui.inst().openGui(gui.getPlayerData(), category, list, 0);
                }
            }
        }
    }
}
