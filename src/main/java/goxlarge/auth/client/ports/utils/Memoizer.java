package goxlarge.auth.client.ports.utils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Memoizer<K, V>
        implements Function<K, V> {
    /**
     * This map associates a key K with a value V that's produced by a
     * function.
     */
    private final Map<K, V> mCache;

    /**
     * This function produces a value based on the key.
     */
    private final Function<K, V> mFunction;

    /**
     * Constructor initializes the fields.
     *
     * @param function The function that produces a value based on a
     *                 key
     * @param map The implementation of {@code Map} used to cache a
     *          value with its associated key
     */
    public Memoizer(Function<K, V> function,
                    Map<K, V> map) {
        mFunction = function;
        mCache = map;
    }

    /**
     * Returns the value associated with the key in cache.  If there's
     * no value associated with the key then the function is called to
     * create the value and store it in the cache before returning it.
     */
    public V apply(final K key) {
        return mCache.computeIfAbsent(key, mFunction);
    }

    /**
     * Removes the key (and its corresponding value) from this
     * memoizer.  This method does nothing if the key is not in the
     * map.
     *
     * @param key The key to remove
     * @ @return The previous value associated with key, or null if
     * there was no mapping for key.
     */
    public V remove(K key) {
        return mCache.remove(key);
    }

    /**
     * @return The number of keys in the cache.
     */
    public long size() {
        return mCache.size();
    }
}