package dev.nklip.javacraft.algs.leetcode.easy;

import dev.nklip.javacraft.algs.leetcode.model.TreeNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConvertSortedArrayToBinarySearchTreeTest {


    @Test
    void testCase1() {
        ConvertSortedArrayToBinarySearchTree solution = new ConvertSortedArrayToBinarySearchTree();

        int[] nums = {-10,-3,0,5,9};
        //
        //         0
        //        / \
        //      -3   9
        //      /   /
        //   -10   5
        //
        TreeNode actualResult = solution.sortedArrayToBST(nums);
        // output : [0,-3,9,-10,null,5]
        //        0
        //      /   \
        //   -10     5
        //      \      \
        //      -3      9
        Assertions.assertEquals(0, actualResult.val);
        Assertions.assertEquals(-10, actualResult.left.val);
        Assertions.assertEquals(-3, actualResult.left.right.val);
        Assertions.assertEquals(5, actualResult.right.val);
        Assertions.assertEquals(9, actualResult.right.right.val);
    }

    @Test
    void testCase2() {
        ConvertSortedArrayToBinarySearchTree solution = new ConvertSortedArrayToBinarySearchTree();

        int[] nums = {1,3};
        TreeNode actualResult = solution.sortedArrayToBST(nums);
        // output : [3,1]
        Assertions.assertEquals(1, actualResult.val);
        Assertions.assertEquals(3, actualResult.right.val);
    }
}
