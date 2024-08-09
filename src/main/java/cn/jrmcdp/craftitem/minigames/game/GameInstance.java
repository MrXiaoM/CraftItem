package cn.jrmcdp.craftitem.minigames.game;

import cn.jrmcdp.craftitem.minigames.GameData;
import org.bukkit.entity.Player;

public interface GameInstance {

    GamingPlayer start(GameData game, Player player, GameSettings settings);
}