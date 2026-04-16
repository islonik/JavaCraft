package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImplementQueueUsingStacksTest {
    @Test
    public void testCase1() {
        ImplementQueueUsingStacks queueImpl = new ImplementQueueUsingStacks();

        Assertions.assertTrue(queueImpl.empty());
        queueImpl.push(1);
        Assertions.assertFalse(queueImpl.empty());
        Assertions.assertEquals(1, queueImpl.peek());
        queueImpl.push(2);
        Assertions.assertEquals(1, queueImpl.peek());
        Assertions.assertEquals(1, queueImpl.pop());
        Assertions.assertEquals(2, queueImpl.peek());
        Assertions.assertFalse(queueImpl.empty());
    }
}
