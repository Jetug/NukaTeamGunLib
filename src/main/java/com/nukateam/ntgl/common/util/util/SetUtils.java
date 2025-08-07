package com.nukateam.ntgl.common.util.util;

import java.util.ArrayList;
import java.util.Set;

public class SetUtils {
    public static <T> T cycleSet(Set<T> set, T value) {
        var buff = new ArrayList<>(set.stream().toList());
        var i = buff.indexOf(value);

        if(i == set.size() - 1)
            i = 0;
        else i++;

        return buff.get(i);
    }

    public static <T> T getFirst(Set<T> set) {
        return set.iterator().next();
    }
}
