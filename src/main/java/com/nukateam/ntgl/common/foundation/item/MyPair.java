package com.nukateam.ntgl.common.foundation.item;

import com.mojang.datafixers.kinds.*;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MyPair<F, S>{
    private F first;
    private S second;

    public MyPair(final F first, final S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof MyPair<?, ?>)) {
            return false;
        }
        final MyPair<?, ?> other = (MyPair<?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(first, second);
    }

    public static <F, S> MyPair<F, S> of(final F first, final S second) {
        return new MyPair<>(first, second);
    }
}
