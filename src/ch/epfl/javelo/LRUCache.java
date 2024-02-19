package ch.epfl.javelo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that represents Least Recently Used (LRU) cache with a certain capacity
 *
 * @param <K> : Generic type for the keys
 * @param <V> : Generic type for the values
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int MAX_ENTRIES;

    /**
     * Constructor
     *
     * @param maxCapacity : maximum capacity of the cache
     * @param loadFactor  the load factor
     */
    public LRUCache(int maxCapacity, float loadFactor) {
        super(maxCapacity, loadFactor, true);
        this.MAX_ENTRIES = maxCapacity;
    }

    /**
     * This method determines whether an element should be removed
     *
     * @param eldest : entry of the map
     * @return true iff the size has exceeded the capacity
     */

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > MAX_ENTRIES;
    }
}

