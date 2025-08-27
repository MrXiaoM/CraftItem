package cn.jrmcdp.craftitem.actions;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigCategoryGui;
import cn.jrmcdp.craftitem.gui.GuiForge;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ActionReopen implements IAction {
    public static final IActionProvider PROVIDER;
    public static final ActionReopen INSTANCE;
    static {
        INSTANCE = new ActionReopen();
        PROVIDER = s -> s.equals("[reopen]") || s.equals("reopen") ? INSTANCE : null;
    }
    private ActionReopen() {
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv == null) return;
        InventoryHolder holder = Utils.getHolder(inv);
        if (holder instanceof GuiForge) {
            GuiForge gui = (GuiForge) holder;
            gui.parent.openGui(gui.getPlayerData(), gui.getId(), gui.getCraftData(), gui.getCategory());
        }
    }
}
