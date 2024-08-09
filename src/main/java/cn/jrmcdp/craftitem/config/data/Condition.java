package cn.jrmcdp.craftitem.config.data;

import cn.jrmcdp.craftitem.utils.PlaceholderSupport;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.entity.Player;

public class Condition {
    public final String input;
    public final String type;
    public final String output;

    public Condition(String input, String type, String output) {
        this.input = input;
        this.type = type;
        this.output = output;
    }

    public boolean check(Player player) {
        String i = PlaceholderSupport.setPlaceholders(player, input);
        String o = PlaceholderSupport.setPlaceholders(player, output);
        boolean reversed = type.startsWith("!");
        String s = reversed ? type.substring(1) : type;
        switch (s) {
            case "=":
            case "==":
            case "string equals":
                return i.equals(o) != reversed;
            case "~==":
            case "equalsIgnoreCase":
                return i.equalsIgnoreCase(o) != reversed;
            case ">=":
            case ">":
            case "<=":
            case "<":
            case "number equals":
                Double nInput = Utils.tryParseDouble(i);
                Double nOutput = Utils.tryParseDouble(o);
                switch (s) {
                    case "number equals":
                        if (nInput == null || nOutput == null) {
                            return i.equals(o) != reversed;
                        } else {
                            return (nInput.equals(nOutput)) != reversed;
                        }
                    case ">=":
                        if (nInput == null || nOutput == null) {
                            return i.equals(o) != reversed;
                        } else {
                            return (nInput >= nOutput) != reversed;
                        }
                    case "<=":
                        if (nInput == null || nOutput == null) {
                            return i.equals(o) != reversed;
                        } else {
                            return (nInput <= nOutput) != reversed;
                        }
                    case ">":
                        if (nInput == null || nOutput == null) {
                            return false;
                        } else {
                            return (nInput > nOutput) != reversed;
                        }
                    case "<":
                        if (nInput == null || nOutput == null) {
                            return false;
                        } else {
                            return (nInput < nOutput) != reversed;
                        }
                    default:
                        return false;
                }
            default:
                return false;
        }
    }
}
