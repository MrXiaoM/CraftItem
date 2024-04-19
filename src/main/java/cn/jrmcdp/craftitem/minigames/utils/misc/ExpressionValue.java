package cn.jrmcdp.craftitem.minigames.utils.misc;

import org.bukkit.entity.Player;
import cn.jrmcdp.craftitem.minigames.utils.ConfigUtils;

import java.util.Map;

public class ExpressionValue implements Value {

    private final String expression;

    public ExpressionValue(String expression) {
        this.expression = expression;
    }

    @Override
    public double get(Player player, Map<String, String> values) {
        return ConfigUtils.getExpressionValue(player, expression, values);
    }
}
