package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 231. Power of Two
 *
 * LeetCode: https://leetcode.com/problems/power-of-two/
 *
 * Given an integer n, return true if it is a power of two. Otherwise, return false.
 * An integer n is a power of two, if there exists an integer x such that n == 2x.
 */
public class PowerOfTwo {

    /*
     * In Java, & has two meanings depending on context:
     *
     * Bitwise AND (on integers)
     * int a = 5;      // 0101
     * int b = 3;      // 0011
     * int c = a & b;  // 0001 → 1
     *
     * Why it works
     * 1) n - 1 flips the lowest set bit to 0 and turns all the 0s below it into 1s
     * 2) & keeps everything above unchanged and zeroes out the lowest set bit and below
     *
     * For example:
     * n = 4 → binary 0100
     *
     * n       = 0100
     * n - 1   = 0011
     * n & n-1 = 0000  → equals 0 → it IS a power of two -> CONFIRMED
     *
     * n = 6 → binary 0110
     *
     * n       = 0110
     * n - 1   = 0101
     * n & n-1 = 0100  → NOT zero → not a power of two -> DENIED
     */
    public boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

}
