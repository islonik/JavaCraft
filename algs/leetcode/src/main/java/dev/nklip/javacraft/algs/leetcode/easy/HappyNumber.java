package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.HashSet;
import java.util.Set;

/*
 * 202. Happy Number
 *
 * LeetCode: https://leetcode.com/problems/happy-number/
 *
 * Write an algorithm to determine if a number n is happy.
 *
 * A happy number is a number defined by the following process:
 *
 * 1) Starting with any positive integer, replace the number by the sum of the squares of its digits.
 * 2) Repeat the process until the number equals 1 (where it will stay),
 * or it loops endlessly in a cycle which does not include 1.
 * 3) Those numbers for which this process ends in 1 are happy.
 *
 * Return true if n is a happy number, and false if not.
 */
public class HappyNumber {

    // Floyd's cycle detection (tortoise & hare) — O(log n) time, O(1) space
    public boolean isHappy(int n) {
        int slow = n;
        int fast = sumOfSquares(n);

        while (fast != 1 && slow != fast) {
            slow = sumOfSquares(slow);
            fast = sumOfSquares(sumOfSquares(fast));
        }
        return fast == 1;
    }

    // HashSet cycle detection — O(log n) time, O(log n) space
    public boolean isHappyUseSet(int n) {
        Set<Integer> seen = new HashSet<>();
        while (n != 1 && seen.add(n)) {
            n = sumOfSquares(n);
        }
        return n == 1;
    }

    private int sumOfSquares(int n) {
        int sum = 0;
        while (n > 0) {
            // take the latest int char
            int d = n % 10;
            // reduce n by 10
            n /= 10;
            // find the sum of squares
            sum += d * d;
        }
        return sum;
    }

}
