package dev.nklip.javacraft.algs.leetcode.medium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LRUCacheTest {

    @Test
    void testCase1() {
        // Explanation
        // LRUCache lRUCache = new LRUCache(2);
        // lRUCache.put(1, 1); // cache is {1=1}
        // lRUCache.put(2, 2); // cache is {1=1, 2=2}
        // lRUCache.get(1);    // return 1
        // lRUCache.put(3, 3); // LRU key was 2, evicts key 2, cache is {1=1, 3=3}
        // lRUCache.get(2);    // returns -1 (not found)
        // lRUCache.put(4, 4); // LRU key was 1, evicts key 1, cache is {4=4, 3=3}
        // lRUCache.get(1);    // return -1 (not found)
        // lRUCache.get(3);    // return 3
        // lRUCache.get(4);    // return 4
        LRUCache lruCache = new LRUCache(2);
        lruCache.put(1, 1); // cache is {1=1}
        lruCache.put(2, 2); // cache is {1=1, 2=2}
        Assertions.assertEquals(1, lruCache.get(1));    // return 1
        lruCache.put(3, 3); // LRU key was 2, evicts key 2, cache is {1=1, 3=3}
        Assertions.assertEquals(-1, lruCache.get(2));    // returns -1 (not found)
        lruCache.put(4, 4); // LRU key was 1, evicts key 1, cache is {4=4, 3=3}
        Assertions.assertEquals(-1, lruCache.get(1));    // return -1 (not found)
        Assertions.assertEquals(3, lruCache.get(3));    // return 3
        Assertions.assertEquals(4, lruCache.get(4));    // return 4
    }

    @Test
    void testCase2() {
        LRUCache lruCache = new LRUCache(2);

        Assertions.assertEquals(-1, lruCache.get(2));

        lruCache.put(2,6);

        Assertions.assertEquals(-1, lruCache.get(1));

        lruCache.put(1,5);
        lruCache.put(1,2);

        Assertions.assertEquals(2, lruCache.get(1));
        Assertions.assertEquals(6, lruCache.get(2));
    }

    @Test
    void testCase3() {
        LRUCache lruCache = new LRUCache(2);

        lruCache.put(2,1);
        lruCache.put(1,1);
        lruCache.put(2,3);
        lruCache.put(4,1);

        Assertions.assertEquals(-1, lruCache.get(1));

        Assertions.assertEquals(3, lruCache.get(2));
    }
}
