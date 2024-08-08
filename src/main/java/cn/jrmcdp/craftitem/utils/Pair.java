package cn.jrmcdp.craftitem.utils;

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
}
