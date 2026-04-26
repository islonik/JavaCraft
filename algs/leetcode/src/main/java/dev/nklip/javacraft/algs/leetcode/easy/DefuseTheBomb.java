package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 1652. Defuse the Bomb
 *
 * LeetCode: https://leetcode.com/problems/defuse-the-bomb/
 *
 * You have a bomb to defuse, and your time is running out!
 * Your informer will provide you with a circular array code of length of n and a key k.
 *
 * To decrypt the code, you must replace every number. All the numbers are replaced simultaneously.
 *
 * 1) If k > 0, replace the ith number with the sum of the next k numbers.
 * 2) If k < 0, replace the ith number with the sum of the previous -k numbers.
 * 3) If k == 0, replace the ith number with 0.
 *
 * As code is circular, the next element of code[n-1] is code[0], and the previous element of code[0] is code[n-1].
 *
 * Given the circular array code and an integer key k, return the decrypted code to defuse the bomb!
 */
public class DefuseTheBomb {

    // Time - O(n); Space - O(n)
    public int[] decrypt(int[] code, int k) {
        if (k == 0) {
            return new int[code.length];
        } else if (k < 0) {
            return negativeDecoding(code, k);
        } else {
            return positiveDecoding(code, k);
        }
    }

    private int[] negativeDecoding(int[] code, int k) {
        int[] decrypted = new int[code.length];

        int module = Math.abs(k);

        int sum = 0;
        for (int i = -module; i < code.length; i++) {
            int value = (i < 0)
                ? code[code.length - Math.abs(i)]
                : code[i];
            if (i >= 0) {
                decrypted[i] = sum;

                sum += value;

                value = (i - module < 0)
                        ? code[code.length - module + i]
                        : code[i - module];
                sum -= value;
            } else {
                sum += value;
            }
        }
        return decrypted;
    }

    private int[] positiveDecoding(int[] code, int k) {
        int[] decrypted = new int[code.length];

        int sum = 0;
        for (int i = code.length - 1 + k; i >= 0; i--) {
            int value = (i > code.length - 1)
                    ? code[Math.abs(i) - code.length]
                    : code[i];
            if (i <= code.length - 1) {
                decrypted[i] = sum;

                sum += value;

                value = (i + k > code.length - 1)
                        ? code[i + k - code.length]
                        : code[i + k];
                sum -= value;
            } else {
                sum += value;
            }
        }
        return decrypted;
    }
}
