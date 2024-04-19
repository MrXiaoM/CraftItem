package cn.jrmcdp.craftitem.minigames.utils.game;

import cn.jrmcdp.craftitem.minigames.GameData;
import cn.jrmcdp.craftitem.minigames.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import cn.jrmcdp.craftitem.minigames.utils.effect.Effect;

public abstract class AbstractGamingPlayer implements GamingPlayer, Runnable {
    protected long deadline;
    protected boolean success;
    protected BukkitTask task;
    protected GameData game;
    protected Player player;
    protected GameSettings settings;
    protected boolean isTimeOut;

    public AbstractGamingPlayer(GameData game, Player player, GameSettings settings) {
        this.game = game;
        this.player = player;
        this.settings = settings;
        this.deadline = (long) (System.currentTimeMillis() + settings.getTime() * 1000L);
        this.arrangeTask();
    }

    public void arrangeTask() {
        this.task = Bukkit.getScheduler().runTaskTimer(GameManager.getPlugin(), this, 1, 1);
    }

    @Override
    public void cancel() {
        if (task != null && !task.isCancelled())
            task.cancel();
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public boolean onRightClick() {
        endGame();
        return true;
    }

    @Override
    public boolean onLeftClick() {
        return false;
    }

    @Override
    public boolean onChat(String message) {
        return false;
    }

    @Override
    public boolean onSwapHand() {
        return false;
    }

    @Override
    public boolean onJump() {
        return false;
    }

    @Override
    public boolean onSneak() {
        return false;
    }

    @Override
    public GameData getGame() {
        return game;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Effect getEffectReward() {
        return null;
    }

    @Override
    public void run() {
        if (timeOutCheck()) {
            return;
        }
        //switchItemCheck();
        onTick();
    }

    public void onTick() {

    }

    protected void endGame() {
        GameManager.inst().processGameResult(this);
    }

    protected void setGameResult(boolean success) {
        this.success = success;
    }

    protected boolean timeOutCheck() {
        if (System.currentTimeMillis() > deadline) {
            isTimeOut = true;
            cancel();
            endGame();
            return true;
        }
        return false;
    }
}
