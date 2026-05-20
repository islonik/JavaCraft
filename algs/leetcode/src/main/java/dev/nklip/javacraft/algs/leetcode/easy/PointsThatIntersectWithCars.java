package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.*;

/*
 * 2848. Points That Intersect With Cars
 *
 * LeetCode: https://leetcode.com/problems/points-that-intersect-with-cars/
 *
 * You are given a 0-indexed 2D integer array nums representing the coordinates of the cars parking on a number line.
 * For any index i, nums[i] = [starti, endi] where starti is the starting point of the ith car and endi is the ending point of the ith car.
 *
 * Return the number of integer points on the line that are covered with any part of a car.
 */
public class PointsThatIntersectWithCars {

    // Time complexity if O(n log n)
    //
    // Space complexity is O(n) for the distinctIntervals list (worst case, no intervals overlap).
    @SuppressWarnings({"replaced", "ComparatorCombinators"})
    public int numberOfPointsSortAndMerge(List<List<Integer>> nums) {
        // below sort approach is faster than nums.sort(Comparator.comparingInt(List::getFirst));
        nums.sort((a, b) -> Integer.compare(a.getFirst(), b.getFirst()));

        // merge overlapping intervals
        List<List<Integer>> distinctIntervals = new ArrayList<>();
        List<Integer> lastInterval = new ArrayList<>();
        lastInterval.add(nums.getFirst().getFirst());
        lastInterval.add(nums.getFirst().getLast());
        distinctIntervals.add(lastInterval);

        for (List<Integer> interval : nums) {
            Integer first = interval.getFirst();
            Integer last = interval.getLast();

            Integer lastLast = lastInterval.getLast();

            // intersection found
            if (first <= lastLast) {
                // modify interval
                if (last > lastLast) {
                    lastInterval.removeLast();

                    lastInterval.add(last);
                }
            } else {
                List<Integer> temp = new ArrayList<>();
                temp.add(first);
                temp.add(last);
                distinctIntervals.add(temp);
                lastInterval = temp;
            }
        }

        int points = 0;
        for (List<Integer> interval : distinctIntervals) {
            Integer first = interval.getFirst();
            Integer last = interval.getLast();

            points += last - first + 1;
        }
        return points;
    }

    // Time Complexity is O(n * k) where:
    //
    // n = number of intervals (nums.size())
    // k = average length of each interval (end - start + 1)
    //
    // Space complexity is O(T) where T is the total number of distinct integer points across all intervals
    // (worst case, the size of the union of all ranges).
    public int numberOfPointsUseSet(List<List<Integer>> nums) {
        Set<Integer> set = new HashSet<>();

        for (List<Integer> interval : nums) {
            Integer first = interval.getFirst();
            Integer last = interval.getLast();

            for (int i = first; i <= last; i++) {
                set.add(i);
            }
        }
        return set.size();
    }

}
