package cn.jrmcdp.craftitem.manager;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.func.AbstractModule;
import cn.jrmcdp.craftitem.func.CraftRecipeManager;
import cn.jrmcdp.craftitem.gui.GuiForge;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.Map;
import java.util.function.BiConsumer;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

@Deprecated
@AutoRegister(priority = 1001)
public class CraftDataManager extends AbstractModule {
    CraftRecipeManager manager;
    public CraftDataManager(CraftItem plugin) {
        super(plugin);
        manager = CraftRecipeManager.inst();
    }

    @Deprecated
    public Map<String, CraftData> getCraftDataMap() {
        return manager.getCraftDataMap();
    }

    @Deprecated
    public CraftData getCraftData(String key) {
        return manager.getCraftData(key);
    }

    @Deprecated
    public void save(String id, CraftData craftData) {
        manager.save(id, craftData);
    }

    @Deprecated
    public void delete(String id) {
        manager.delete(id);
    }

    @Deprecated
    public boolean doForgeResult(GuiForge holder, Player player, boolean win, int multiple, Runnable cancel) {
        return manager.doForgeResult(holder, player, win, multiple, cancel);
    }

    @Deprecated
    public void playForgeAnimate(Player player, BiConsumer<Runnable, Runnable> consumer) {
        manager.playForgeAnimate(player, consumer);
    }

    public static CraftDataManager inst() {
        return instanceOf(CraftDataManager.class);
    }
}
