package org.example;

import com.sun.deploy.cache.CacheEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleCacheManager<K, V> {

    private final Map<K, CacheEntry<V>> cache;
    private final long defaultExpirationTime;  // in milliseconds

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SimpleCacheManager(long defaultExpirationTime) {
        this.cache = new HashMap<>();
        this.defaultExpirationTime = defaultExpirationTime;
        scheduler.scheduleAtFixedRate(this::removeExpiredKeys, 0, 1, TimeUnit.SECONDS);

    }

    public void put(K key, V value) {
        put(key, value, defaultExpirationTime);
    }

    public void put(K key, V value, long expirationTime) {
        long expirationTimestamp = System.currentTimeMillis() + expirationTime;
        cache.put(key, new CacheEntry<>(value, expirationTimestamp));
    }

    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);

        if (entry != null && entry.isValid()) {
            return entry.getValue();
        } else {
            // Remove entry if it's expired or not found
            cache.remove(key);
            return null;
        }
    }

    public void removeExpiredKeys () {
        for(Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isValid()) {
                K key = entry.getKey();
                cache.remove(key);
                System.out.println("Removing expired " + entry.getValue().getValue());
                System.out.println("Expired Key Present " + cache.get(key));
            }
        }
    }

    private static class CacheEntry<T> {
        private final T value;
        private final long expirationTimestamp;

        public CacheEntry(T value, long expirationTimestamp) {
            this.value = value;
            this.expirationTimestamp = expirationTimestamp;
        }

        public T getValue() {
            return value;
        }

        public boolean isValid() {
            return System.currentTimeMillis() <= expirationTimestamp;
        }
    }

    public static void main(String[] args) {
        // Example Usage:
        SimpleCacheManager<String, Integer> cacheManager = new SimpleCacheManager<>(5000); // 5 seconds

        cacheManager.put("key1", 42);
        cacheManager.put("key2", 100, 10000); // 10 seconds

        System.out.println(cacheManager.get("key1")); // Output: 42

//        // Wait for 6 seconds (key2 will expire)
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        System.out.println(cacheManager.get("key2")); // Output: null (expired)
    }
}