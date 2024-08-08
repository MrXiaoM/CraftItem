package cn.jrmcdp.craftitem.utils;

public class Triple<K, V, T> {
    public final K first;
    public final V second;
    public final T third;

    Triple(K first, V second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static <K, V, T> Triple<K, V, T> of(K first, V second, T third) {
        return new Triple<>(first, second, third);
    }
}
