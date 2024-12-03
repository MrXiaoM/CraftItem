package cn.jrmcdp.craftitem.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pair<K, V> {
    public final K key;
    public final V value;
    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public K left() {
        return key;
    }

    public V right() {
        return value;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    @SafeVarargs
    public static List<String> replace(Collection<String> oldList, Pair<String, Object>... replacements) {
        List<String> list = new ArrayList<>();
        for (String s : oldList) {
            for (Pair<String, Object> pair : replacements) {
                String key = pair.getKey();
                String value = String.valueOf(pair.getValue());
                s = s.replace(key, value);
            }
            list.add(s);
        }
        return list;
    }
}
