package cn.jrmcdp.craftitem.minigames;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnPaper implements Listener {
    PlayerJump playerJump;
    public interface PlayerJump {
        void run(Cancellable event, Player player);
    }
    public OnPaper(PlayerJump playerJump) {
        this.playerJump = playerJump;
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        playerJump.run(event, event.getPlayer());
    }
}
