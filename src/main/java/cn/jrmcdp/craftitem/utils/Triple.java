package cn.jrmcdp.craftitem.utils;

public class Triple<K, V, T> {
    K first;
    V second;
    T third;

    Triple(K first, V second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }

    public T getThird() {
        return third;
    }

    public void setThird(T third) {
        this.third = third;
    }

    public static <K, V, T> Triple<K, V, T> of(K first, V second, T third) {
        return new Triple<>(first, second, third);
    }
}
