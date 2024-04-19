package cn.jrmcdp.craftitem.minigames.utils.effect;

import cn.jrmcdp.craftitem.minigames.utils.Pair;
import cn.jrmcdp.craftitem.minigames.utils.misc.WeightModifier;

import java.util.List;

public interface Effect {

    boolean canLavaFishing();

    double getMultipleLootChance();

    double getSize();

    double getSizeMultiplier();

    double getScore();

    double getScoreMultiplier();

    double getWaitTime();

    double getWaitTimeMultiplier();

    double getGameTime();

    double getGameTimeMultiplier();

    double getDifficulty();

    double getDifficultyMultiplier();

    List<Pair<String, WeightModifier>> getWeightModifier();

    List<Pair<String, WeightModifier>> getWeightModifierIgnored();

    void merge(Effect effect);
}
