package com.alfray.conductor.util;

public class Pair<F, S> {
    public final F mFirst;
    public final S mSecond;

    private Pair(F first, S second) {
        mFirst = first;
        mSecond = second;
    }

    public static <U, V> Pair<U, V> of(U first, V second) {
        return new Pair<>(first, second);
    }
}
