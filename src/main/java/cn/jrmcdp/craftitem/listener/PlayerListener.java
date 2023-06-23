package cn.jrmcdp.craftitem.listener;

import cn.jrmcdp.craftitem.manager.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        DataManager.playerDataHashMap.remove(event.getPlayer().getName());
    }

}
