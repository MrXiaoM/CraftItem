package cn.jrmcdp.craftitem.minigames.utils;

import com.google.common.collect.Lists;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for configuration-related operations.
 */
public class ConfigUtils {

    private ConfigUtils() {}

    /**
     * Converts an object into an ArrayList of strings.
     *
     * @param object The input object
     * @return An ArrayList of strings
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<String> stringListArgs(Object object) {
        ArrayList<String> list = new ArrayList<>();
        if (object instanceof String) {
            list.add((String) object);
        } else if (object instanceof List<?>) {
            list.addAll((Collection<? extends String>) object);
        } else if (object instanceof String[]) {
            list.addAll(Lists.newArrayList((String[]) object));
        }
        return list;
    }

    /**
     * Parses a string representing a size range and returns a pair of ints.
     *
     * @param string The size string in the format "min~max".
     * @return A pair of ints representing the minimum and maximum size.
     */
    public static Pair<Integer, Integer> getIntegerPair(String string) {
        if (string == null) return null;
        String[] split = string.split("~", 2);
        if (split.length != 2) {
            LogUtils.warn("Illegal size argument: " + string);
            LogUtils.warn("Correct usage example: 10~20");
            throw new IllegalArgumentException("Illegal int range");
        }
        return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

}
