package cn.jrmcdp.craftitem.minigames.game;

import cn.jrmcdp.craftitem.minigames.GameData;
import cn.jrmcdp.craftitem.minigames.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public abstract class AbstractGamingPlayer implements GamingPlayer, Runnable {
    protected final GameData game;
    protected final Player player;
    protected final GameSettings settings;
    protected final long deadline;
    protected BukkitTask task;
    protected boolean success;
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
        if (task != null) {
            task.cancel();
            task = null;
        }
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
