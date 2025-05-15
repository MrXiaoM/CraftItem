package cn.jrmcdp.craftitem.manager;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.func.AbstractModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.HashMap;
import java.util.Map;

@Deprecated
@AutoRegister(priority = 1001)
public class PlayerDataManager extends AbstractModule {
    cn.jrmcdp.craftitem.func.PlayerDataManager manager;
    public PlayerDataManager(CraftItem plugin) {
        super(plugin);
        manager = cn.jrmcdp.craftitem.func.PlayerDataManager.inst();
    }

    @Deprecated
    public PlayerData getOrCreatePlayerData(Player player) {
        return manager.getOrCreatePlayerData(player);
    }

    public static PlayerDataManager inst() {
        return instanceOf(PlayerDataManager.class);
    }
}
