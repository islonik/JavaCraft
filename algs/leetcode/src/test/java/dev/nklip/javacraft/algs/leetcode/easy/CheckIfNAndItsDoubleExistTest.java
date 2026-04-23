package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckIfNAndItsDoubleExistTest {

    @Test
    void testCase1() {
        CheckIfNAndItsDoubleExist solution = new CheckIfNAndItsDoubleExist();

        // Explanation: For i = 0 and j = 2, arr[i] == 10 == 2 * 5 == 2 * arr[j]
        Assertions.assertTrue(solution.checkIfExist(new int[]{10,2,5,3}));
        Assertions.assertTrue(solution.checkIfExistTwoPointers(new int[]{10,2,5,3}));

        Assertions.assertFalse(solution.checkIfExist(new int[]{0, 2, -2}));
    }

    @Test
    void testCase2() {
        CheckIfNAndItsDoubleExist solution = new CheckIfNAndItsDoubleExist();

        // Explanation: There is no i and j that satisfy the conditions.
        Assertions.assertFalse(solution.checkIfExist(new int[]{3,1,7,11}));
        Assertions.assertFalse(solution.checkIfExistTwoPointers(new int[]{3,1,7,11}));
    }

    @Test
    void testCase3() {
        CheckIfNAndItsDoubleExist solution = new CheckIfNAndItsDoubleExist();

        Assertions.assertTrue(solution.checkIfExist(new int[]{-20,8,-6,-14,0,-19,14,4}));
        Assertions.assertTrue(solution.checkIfExistTwoPointers(new int[]{-20,8,-6,-14,0,-19,14,4}));
    }

    @Test
    void testCase4() {
        CheckIfNAndItsDoubleExist solution = new CheckIfNAndItsDoubleExist();

        Assertions.assertTrue(solution.checkIfExist(new int[]{-10,12,-20,-8,15}));
        Assertions.assertTrue(solution.checkIfExistTwoPointers(new int[]{-10,12,-20,-8,15}));
    }

    @Test
    void testCase6() {
        CheckIfNAndItsDoubleExist solution = new CheckIfNAndItsDoubleExist();

        Assertions.assertTrue(solution.checkIfExist(new int[]{0,0}));
        Assertions.assertTrue(solution.checkIfExistTwoPointers(new int[]{0,0}));
    }

    @Test
    void testCase7() {
        CheckIfNAndItsDoubleExist solution = new CheckIfNAndItsDoubleExist();

        Assertions.assertTrue(solution.checkIfExistTwoPointers(
                new int[]{-766,259,203,601,896,-226,-844,168,126,-542,159,-833,950,-454,-253,824,-395,155,94,894,-766,-63,836,-433,-780,611,-907,695,-395,-975,256,373,-971,-813,-154,-765,691,812,617,-919,-616,-510,608,201,-138,-669,-764,-77,-658,394,-506,-675,523}));
    }
}
