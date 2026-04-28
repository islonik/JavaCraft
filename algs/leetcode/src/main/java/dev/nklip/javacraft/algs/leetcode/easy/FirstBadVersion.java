package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 278. First Bad Version
 *
 * LeetCode: https://leetcode.com/problems/first-bad-version/
 *
 * You are a product manager and currently leading a team to develop a new product.
 * Unfortunately, the latest version of your product fails the quality check.
 * Since each version is developed based on the previous version, all the versions after a bad version are also bad.
 *
 * Suppose you have n versions [1, 2, ..., n] and you want to find out the first bad one,
 * which causes all the following ones to be bad.
 *
 * You are given an API bool isBadVersion(version) which returns whether version is bad.
 * Implement a function to find the first bad version. You should minimize the number of calls to the API.
 */
public class FirstBadVersion {

    private final int firstBadVersion;

    public FirstBadVersion(int badVersion) {
        firstBadVersion = badVersion;
    }

    boolean isBadVersion(int version) {
        return firstBadVersion <= version;
    }

    public int firstBadVersion(int n) {
        return firstBadVersion(1, n, -1);
    }

    private int firstBadVersion(int min, int max, int prevBadVersion) {
        int mid = min + (max - min) / 2;

        if (isBadVersion(mid)) {
            if (max - min == 1 || max - min == 0){
                return mid;
            }

            // first bad version could be earlier
            return firstBadVersion(min, mid - 1, mid);
        } else if (prevBadVersion > -1) {
            // first version was earlier
            if (max - min == 1 || max - min == 0){
                return prevBadVersion;
            } else {
                return firstBadVersion(mid + 1, max, prevBadVersion);
            }
        } else {
            // first bad version should be later
            return firstBadVersion(mid + 1, max, -1);
        }

    }
}
