package cn.jrmcdp.craftitem.utils;

import java.util.List;
import java.util.Random;

public class RandomUtils {
    private static final Random SHARED_RANDOM = new Random();

    private RandomUtils() {
    }

    public static <T> T next(List<T> list, T def) {
        int size = list.size();
        if (size == 0) return def;
        if (size == 1) return list.get(0);
        return list.get(nextInt(list.size()));
    }

    public static int nextInt() {
        return nextInt(SHARED_RANDOM);
    }

    public static int nextInt(Random random) {
        return random.nextInt();
    }

    public static int nextInt(int n) {
        return nextInt(SHARED_RANDOM, n);
    }

    public static int nextInt(Random random, int n) {
        return random.nextInt(n);
    }
}
