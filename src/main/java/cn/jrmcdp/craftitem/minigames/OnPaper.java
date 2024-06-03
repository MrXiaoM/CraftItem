package cn.jrmcdp.craftitem.minigames;

import cn.jrmcdp.craftitem.minigames.utils.game.GamingPlayer;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnPaper implements Listener {
    GameManager manager;
    public OnPaper(GameManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        if (event.isCancelled()) return;
        GamingPlayer gamingPlayer = manager.gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onJump())
                event.setCancelled(true);
        }
    }
}
