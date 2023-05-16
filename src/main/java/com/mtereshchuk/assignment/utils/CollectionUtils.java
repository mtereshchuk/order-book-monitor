package com.mtereshchuk.assignment.utils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableMap;

/**
 * @author mtereshchuk
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <K, V> Map<K, V> immutableCopyOf(SortedMap<K, V> map, int size) {
        var copy = new TreeMap<K, V>(map.comparator());
        var iterator = map.entrySet().iterator();
        while (iterator.hasNext() && copy.size() < size) {
            var entry = iterator.next();
            copy.put(entry.getKey(), entry.getValue());
        }
        return unmodifiableMap(copy);
    }
}
