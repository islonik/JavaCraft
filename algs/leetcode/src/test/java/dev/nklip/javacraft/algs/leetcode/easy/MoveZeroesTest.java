package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MoveZeroesTest {
    @Test
    public void testCase1() {
        MoveZeroes solution = new MoveZeroes();

        int[] nums = new int[]{0};
        solution.moveZeroes(nums);

        Assertions.assertEquals(0, nums[0]);
    }

    @Test
    public void testCase2() {
        MoveZeroes solution = new MoveZeroes();

        int[] nums = new int[]{1};
        solution.moveZeroes(nums);

        Assertions.assertEquals(1, nums[0]);
    }

    @Test
    public void testCase3() {
        MoveZeroes solution = new MoveZeroes();

        int[] nums = new int[]{0,1,0,3,12};
        solution.moveZeroes(nums);

        Assertions.assertEquals(1, nums[0]);
        Assertions.assertEquals(3, nums[1]);
        Assertions.assertEquals(12, nums[2]);
    }

    @Test
    public void testCase4() {
        MoveZeroes solution = new MoveZeroes();

        int[] nums = new int[]{1,0};
        solution.moveZeroes(nums);

        Assertions.assertEquals(1, nums[0]);
        Assertions.assertEquals(0, nums[1]);
    }
}
