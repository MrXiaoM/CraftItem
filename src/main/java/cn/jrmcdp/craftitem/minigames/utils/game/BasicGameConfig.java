package cn.jrmcdp.craftitem.minigames.utils.game;

import org.jetbrains.annotations.Nullable;
import cn.jrmcdp.craftitem.minigames.utils.effect.Effect;

import java.util.concurrent.ThreadLocalRandom;

public class BasicGameConfig {

    private int minTime;
    private int maxTime;
    private int minDifficulty;
    private int maxDifficulty;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final BasicGameConfig basicGameConfig;

        public Builder() {
            basicGameConfig = new BasicGameConfig();
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder difficulty(int value) {
            basicGameConfig.minDifficulty = (basicGameConfig.maxDifficulty = value);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder difficulty(int min, int max) {
            basicGameConfig.minDifficulty = min;
            basicGameConfig.maxDifficulty = max;
            return this;
        }

        public Builder time(int value) {
            basicGameConfig.minTime = (basicGameConfig.maxTime = value);
            return this;
        }

        public Builder time(int min, int max) {
            basicGameConfig.minTime = min;
            basicGameConfig.maxTime = max;
            return this;
        }

        public BasicGameConfig build() {
            return basicGameConfig;
        }
    }

    /**
     * Generates random game settings based on specified time and difficulty ranges, adjusted by an effect's difficulty modifier.
     *
     * @param effect The effect to adjust the difficulty.
     * @return A {@link GameSettings} object representing the generated game settings.
     */
    @Nullable
    public GameSettings getGameSetting(Effect effect) {
        return new GameSettings(
                ThreadLocalRandom.current().nextInt(minTime, maxTime + 1) * effect.getGameTimeMultiplier() + effect.getGameTime(),
                (int) Math.min(100, Math.max(1, ThreadLocalRandom.current().nextInt(minDifficulty, maxDifficulty + 1) * effect.getDifficultyMultiplier() + effect.getDifficulty()))
        );
    }
}
