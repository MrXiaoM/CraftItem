package cn.jrmcdp.craftitem.minigames;

import cn.jrmcdp.craftitem.config.Craft;
import cn.jrmcdp.craftitem.holder.ForgeHolder;
import cn.jrmcdp.craftitem.minigames.game.GamingPlayer;
import org.bukkit.entity.Player;

public class GameData {
    public final ForgeHolder holder;
    public final Player player;
    public final boolean win;
    public final int multiple;

    public GameData(ForgeHolder holder, Player player, boolean win, int multiple) {
        this.holder = holder;
        this.player = player;
        this.win = win;
        this.multiple = multiple;
    }

    public void success(GamingPlayer gamingPlayer) {
        if (gamingPlayer.getPlayer().getName().equals(player.getName())) {
            Craft.doForgeResult(holder, player, win, multiple, () -> {});
        }
    }

    public void fail(GamingPlayer gamingPlayer) {
        if (gamingPlayer.getPlayer().getName().equals(player.getName())) {
            Craft.doForgeResult(holder, player, false, multiple, () -> {});
        }
    }
}
