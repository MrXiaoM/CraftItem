package cn.jrmcdp.craftitem.actions;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigCategoryGui;
import cn.jrmcdp.craftitem.gui.GuiForge;
import org.bukkit.entity.HumanEntity;
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
        PROVIDER = s -> s.equals("[back]") || s.equals("back") ? INSTANCE : null;
    }
    private ActionBack() {
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv == null) return;
        InventoryHolder holder = inv.getHolder();
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
