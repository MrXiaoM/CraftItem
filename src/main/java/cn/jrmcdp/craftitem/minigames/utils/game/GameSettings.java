package cn.jrmcdp.craftitem.minigames.utils.game;

public class GameSettings {

    private final double time;
    private final int difficulty;

    public GameSettings(double time, int difficulty) {
        this.time = time;
        this.difficulty = difficulty;
    }

    public double getTime() {
        return time;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
