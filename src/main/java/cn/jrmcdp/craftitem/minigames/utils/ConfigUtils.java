package cn.jrmcdp.craftitem.minigames.utils;

import com.google.common.collect.Lists;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cn.jrmcdp.craftitem.minigames.utils.misc.ExpressionValue;
import cn.jrmcdp.craftitem.minigames.utils.misc.PlainValue;
import cn.jrmcdp.craftitem.minigames.utils.misc.Value;
import cn.jrmcdp.craftitem.minigames.utils.misc.WeightModifier;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for configuration-related operations.
 */
public class ConfigUtils {
    static Pattern pattern = Pattern.compile("\\{[^{}]+}");;

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
     * Splits a string into a pair of integers using the "~" delimiter.
     *
     * @param value The input string
     * @return A Pair of integers
     */
    public static Pair<Integer, Integer> splitStringIntegerArgs(String value, String regex) {
        String[] split = value.split(regex);
        return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    /**
     * Converts a list of strings in the format "key:value" into a list of Pairs with keys and doubles.
     *
     * @param list The input list of strings
     * @return A list of Pairs containing keys and doubles
     */
    public static List<Pair<String, Double>> getWeights(List<String> list) {
        List<Pair<String, Double>> result = new ArrayList<>(list.size());
        for (String member : list) {
            String[] split = member.split(":",2);
            String key = split[0];
            result.add(Pair.of(key, Double.parseDouble(split[1])));
        }
        return result;
    }

    /**
     * Converts an object into a double value.
     *
     * @param arg The input object
     * @return A double value
     */
    public static double getDoubleValue(Object arg) {
        if (arg instanceof Double) {
            return (Double) arg;
        } else if (arg instanceof Integer) {
            return (int) arg;
        }
        return 0;
    }

    /**
     * Converts an object into an integer value.
     *
     * @param arg The input object
     * @return An integer value
     */
    public static int getIntegerValue(Object arg) {
        if (arg instanceof Integer) {
            return (Integer) arg;
        } else if (arg instanceof Double) {
            return ((Double) arg).intValue();
        }
        return 0;
    }

    /**
     * Converts an object into a "value".
     *
     * @param arg int / double / expression
     * @return Value
     */
    public static Value getValue(Object arg) {
        if (arg instanceof Integer) {
            return new PlainValue((int) arg);
        } else if (arg instanceof Double) {
            return new PlainValue((double) arg);
        } else if (arg instanceof String ) {
            return new ExpressionValue((String) arg);
        }
        throw new IllegalArgumentException("Illegal value type");
    }

    /**
     * Parses a string representing a size range and returns a pair of floats.
     *
     * @param string The size string in the format "min~max".
     * @return A pair of floats representing the minimum and maximum size.
     */
    @Nullable
    public static Pair<Float, Float> getFloatPair(String string) {
        if (string == null) return null;
        String[] split = string.split("~", 2);
        if (split.length != 2) {
            LogUtils.warn("Illegal size argument: " + string);
            LogUtils.warn("Correct usage example: 10.5~25.6");
            throw new IllegalArgumentException("Illegal float range");
        }
        return Pair.of(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
    }

    /**
     * Parses a string representing a size range and returns a pair of ints.
     *
     * @param string The size string in the format "min~max".
     * @return A pair of ints representing the minimum and maximum size.
     */
    @Nullable
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

    /**
     * Converts a list of strings in the format "key:value" into a list of Pairs with keys and WeightModifiers.
     *
     * @param modList The input list of strings
     * @return A list of Pairs containing keys and WeightModifiers
     */
    public static List<Pair<String, WeightModifier>> getModifiers(List<String> modList) {
        List<Pair<String, WeightModifier>> result = new ArrayList<>(modList.size());
        for (String member : modList) {
            String[] split = member.split(":",2);
            String key = split[0];
            result.add(Pair.of(key, getModifier(split[1])));
        }
        return result;
    }

    /**
     * Retrieves a list of enchantment pairs from a configuration section.
     *
     * @param section The configuration section to extract enchantment data from.
     * @return A list of enchantment pairs.
     */
    @NotNull
    public static List<Pair<String, Short>> getEnchantmentPair(ConfigurationSection section) {
        List<Pair<String, Short>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof Integer) {
                list.add(Pair.of(entry.getKey(), Short.parseShort(String.valueOf(Math.max(1, Math.min(Short.MAX_VALUE, (Integer) entry.getValue()))))));
            }
        }
        return list;
    }

    public static List<Pair<Integer, Value>> getEnchantAmountPair(ConfigurationSection section) {
        List<Pair<Integer, Value>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            list.add(Pair.of(Integer.parseInt(entry.getKey()), getValue(entry.getValue())));
        }
        return list;
    }

    public static List<Pair<Pair<String, Short>, Value>> getEnchantPoolPair(ConfigurationSection section) {
        List<Pair<Pair<String, Short>, Value>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            list.add(Pair.of(getEnchantmentPair(entry.getKey()), getValue(entry.getValue())));
        }
        return list;
    }

    public static Pair<String, Short> getEnchantmentPair(String value) {
        int last = value.lastIndexOf(":");
        if (last == -1 || last == 0 || last == value.length() - 1) {
            throw new IllegalArgumentException("Invalid format of the input enchantment");
        }
        return Pair.of(value.substring(0, last), Short.parseShort(value.substring(last + 1)));
    }

    /**
     * Retrieves a list of enchantment tuples from a configuration section.
     *
     * @param section The configuration section to extract enchantment data from.
     * @return A list of enchantment tuples.
     */
    @NotNull
    public static List<Tuple<Double, String, Short>> getEnchantmentTuple(ConfigurationSection section) {
        List<Tuple<Double, String, Short>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                ConfigurationSection inner = (ConfigurationSection) entry.getValue();
                Tuple<Double, String, Short> tuple = Tuple.of(
                        inner.getDouble("chance"),
                        inner.getString("enchant"),
                        Short.valueOf(String.valueOf(inner.getInt("level")))
                );
                list.add(tuple);
            }
        }
        return list;
    }

    public static String getString(Object o) {
        if (o instanceof String) {
            return (String) o;
        } else if (o instanceof Integer) {
            return String.valueOf((int) o);
        } else if (o instanceof Double) {
            return String.valueOf((double) o);
        }
        throw new IllegalArgumentException("Illegal string format: " + o);
    }

    /**
     * Reads data from a YAML configuration file and creates it if it doesn't exist.
     *
     * @param file The file path
     * @return The YamlConfiguration
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static YamlConfiguration readData(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.warn("Failed to generate data files!</red>");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Parses a WeightModifier from a string representation.
     *
     * @param text The input string
     * @return A WeightModifier based on the provided text
     * @throws IllegalArgumentException if the weight format is invalid
     */
    public static WeightModifier getModifier(String text) {
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Weight format is invalid.");
        }
        switch (text.charAt(0)) {
            case '/': {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight / arg;
            }
            case '*': {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight * arg;
            }
            case '-': {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight - arg;
            }
            case '%': {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight % arg;
            }
            case '+': {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight + arg;
            }
            case '=': {
                String formula = text.substring(1);
                return (player, weight) -> getExpressionValue(player, formula, new HashMap<String, String>(){{
                    put("{0}", String.valueOf(weight));
                }});
            }
            default: throw new IllegalArgumentException("Invalid weight: " + text);
        }
    }

    public static double getExpressionValue(Player player, String formula, Map<String, String> vars) {
        formula = parse(player, formula, vars);
        return new ExpressionBuilder(formula).build().evaluate();
    }

    /**
     * Parse a text string by replacing placeholders with their corresponding values.
     *
     * @param player      The offline player for whom the placeholders are being resolved (nullable).
     * @param text        The text string containing placeholders.
     * @param placeholders A map of placeholders to their corresponding values.
     * @return The text string with placeholders replaced by their values.
     */
    public static String parse(@Nullable OfflinePlayer player, String text, Map<String, String> placeholders) {
        List<String> list = detectPlaceholders(text);
        for (String papi : list) {
            String replacer = null;
            if (placeholders != null) {
                replacer = placeholders.get(papi);
            }
            if (replacer != null) {
                text = text.replace(papi, replacer);
            }
        }
        return text;
    }

    /**
     * Parse a list of text strings by replacing placeholders with their corresponding values.
     *
     * @param player       The player for whom the placeholders are being resolved (can be null for offline players).
     * @param list         The list of text strings containing placeholders.
     * @param replacements A map of custom replacements for placeholders.
     * @return The list of text strings with placeholders replaced by their values.
     */
    public static List<String> parse(@Nullable OfflinePlayer player, List<String> list, Map<String, String> replacements) {
        return list.stream()
                .map(s -> parse(player, s, replacements))
                .collect(Collectors.toList());
    }
    public static List<String> detectPlaceholders(String text) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }
    public static ArrayList<String> getReadableSection(Map<String, Object> map) {
        ArrayList<String> list = new ArrayList<>();
        mapToReadableStringList(map, list, 0, false);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static void mapToReadableStringList(Map<String, Object> map, List<String> readableList, int loop_times, boolean isMapList) {
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object nbt = entry.getValue();
            if (nbt instanceof String) {
                String value = (String) nbt;
                if (isMapList && first) {
                    first = false;
                    readableList.add(repeat("  ", loop_times - 1) + "<white>- <gold>" + entry.getKey() + ": <white>" + value);
                } else {
                    readableList.add(repeat("  ", loop_times) + "<gold>" + entry.getKey() + ": <white>" + value);
                }
            } else if (nbt instanceof List<?>) {
                List<?> list = (List<?>) nbt;
                if (isMapList && first) {
                    first = false;
                    readableList.add(repeat("  ", loop_times - 1) + "<white>- <gold>" + entry.getKey() + ":");
                } else {
                    readableList.add(repeat("  ", loop_times) + "<gold>" + entry.getKey() + ":");
                }
                for (Object value : list) {
                    if (value instanceof Map<?,?>) {
                        Map<?, ?> nbtMap = (Map<?, ?>) value;
                        mapToReadableStringList((Map<String, Object>) nbtMap, readableList, loop_times + 2, true);
                    } else {
                        readableList.add(repeat("  ", loop_times + 1) + "<white>- " + value);
                    }
                }
            } else if (nbt instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) nbt;
                if (isMapList && first) {
                    first = false;
                    readableList.add(repeat("  ", loop_times - 1) + "<white>- <gold>" + entry.getKey() + ":");
                } else {
                    readableList.add(repeat("  ", loop_times) + "<gold>" + entry.getKey() + ":");
                }
                mapToReadableStringList(section.getValues(false), readableList, loop_times + 1, false);
            } else if (nbt instanceof Map<?,?>) {
                Map<?,?> innerMap = (Map<?, ?>) nbt;
                if (isMapList && first) {
                    first = false;
                    readableList.add(repeat("  ", loop_times - 1) + "<white>- <gold>" + entry.getKey() + ":");
                } else {
                    readableList.add(repeat("  ", loop_times) + "<gold>" + entry.getKey() + ":");
                }
                mapToReadableStringList((Map<String, Object>) innerMap, readableList, loop_times + 1, false);
            }
        }
    }

    public static String repeat(String s, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = count; i > 0; i--) {
            builder.append(s);
        }
        return builder.toString();
    }
}
