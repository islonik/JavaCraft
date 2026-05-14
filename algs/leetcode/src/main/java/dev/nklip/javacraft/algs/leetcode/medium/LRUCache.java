package dev.nklip.javacraft.algs.leetcode.medium;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * 146. LRU Cache
 *
 * LeetCode: https://leetcode.com/problems/lru-cache/description/
 *
 * Design a data structure that follows the constraints of a Least Recently Used (LRU) cache.
 *
 * Implement the LRUCache class:
 *
 * 1) LRUCache(int capacity) Initialize the LRU cache with positive size capacity.
 * 2) int get(int key) Return the value of the key if the key exists, otherwise return -1.
 * 3) void put(int key, int value) Update the value of the key if the key exists.
 *      Otherwise, add the key-value pair to the cache. If the number of keys exceeds the capacity from this operation,
 *      evict the least recently used key.
 *
 * The functions get and put must each run in O(1) average time complexity.
 */
public class LRUCache {
    private final LinkedHashMap<Integer, Integer> map;

    // Space - O(n)
    public LRUCache(int capacity) {
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            // This method was specifically created to use LinkedHashMap for cache purposes
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    // Time - O(1)
    int get(int key) {
        return map.getOrDefault(key, -1);
    }

    // Time - O(1)
    void put(int key, int value) {
        map.put(key, value);
    }
}
