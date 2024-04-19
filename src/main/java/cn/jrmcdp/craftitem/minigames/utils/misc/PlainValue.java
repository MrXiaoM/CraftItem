package cn.jrmcdp.craftitem.minigames.utils.misc;

import org.bukkit.entity.Player;

import java.util.Map;

public class PlainValue implements Value {

    private final double value;

    public PlainValue(double value) {
        this.value = value;
    }

    @Override
    public double get(Player player, Map<String, String> values) {
        return value;
    }
}