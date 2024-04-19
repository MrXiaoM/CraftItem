package cn.jrmcdp.craftitem.minigames.utils.misc;

import org.bukkit.entity.Player;

public interface WeightModifier {

    double modify(Player player, double weight);
}
