package cn.jrmcdp.craftitem.minigames;

import cn.jrmcdp.craftitem.gui.GuiForge;
import cn.jrmcdp.craftitem.minigames.game.GamingPlayer;
import org.bukkit.entity.Player;

public class GameData {
    public final GuiForge holder;
    public final Player player;
    public final boolean win;
    public final int multiple;

    public GameData(GuiForge holder, Player player, boolean win, int multiple) {
        this.holder = holder;
        this.player = player;
        this.win = win;
        this.multiple = multiple;
    }

    public void success(GamingPlayer gamingPlayer) {
        if (gamingPlayer.getPlayer().getName().equals(player.getName())) {
            holder.manager.doForgeResult(holder, player, win, multiple, () -> {});
        }
    }

    public void fail(GamingPlayer gamingPlayer) {
        if (gamingPlayer.getPlayer().getName().equals(player.getName())) {
            holder.manager.doForgeResult(holder, player, false, multiple, () -> {});
        }
    }
}
