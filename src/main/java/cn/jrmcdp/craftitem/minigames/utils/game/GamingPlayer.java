package cn.jrmcdp.craftitem.minigames.utils.game;

import cn.jrmcdp.craftitem.minigames.GameData;
import org.bukkit.entity.Player;

public interface GamingPlayer {

    /**
     * Cancel the game
     */
    void cancel();

    boolean isSuccessful();

    /**
     * @return whether to cancel the event
     */
    boolean onRightClick();

    /**
     * @return whether to cancel the event
     */
    boolean onSwapHand();

    /**
     * @return whether to cancel the event
     */
    boolean onLeftClick();

    /**
     * @return whether to cancel the event
     */
    boolean onChat(String message);

    /**
     * @return whether to cancel the event
     */
    boolean onJump();

    /**
     * @return whether to cancel the event
     */
    boolean onSneak();

    Player getPlayer();

    GameData getGame();
}