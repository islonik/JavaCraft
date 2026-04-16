package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImplementStackUsingQueuesTest {

    @Test
    void testCase1() {
        ImplementStackUsingQueues<Integer> stackImpl = new ImplementStackUsingQueues<>();

        Assertions.assertTrue(stackImpl.empty());

        stackImpl.push(1);
        Assertions.assertEquals(1, stackImpl.top());
        Assertions.assertFalse(stackImpl.empty());

        stackImpl.push(2);
        Assertions.assertEquals(2, stackImpl.top());
        Assertions.assertFalse(stackImpl.empty());

        stackImpl.push(3);
        Assertions.assertEquals(3, stackImpl.top());
        Assertions.assertFalse(stackImpl.empty());

        Assertions.assertEquals(3, stackImpl.pop());
        Assertions.assertEquals(2, stackImpl.pop());
        Assertions.assertEquals(1, stackImpl.top());
        Assertions.assertFalse(stackImpl.empty());
    }

}
