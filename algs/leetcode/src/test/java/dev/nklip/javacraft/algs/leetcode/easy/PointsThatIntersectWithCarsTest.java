package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PointsThatIntersectWithCarsTest {

    @Test
    void testCase1() {
        PointsThatIntersectWithCars solution = new PointsThatIntersectWithCars();

        List<List<Integer>> list = new ArrayList<>();
        list.add(List.of(3,6));
        list.add(List.of(1,5));
        list.add(List.of(4,7));
        // Explanation: All the points from 1 to 7 intersect at least one car, therefore the answer would be 7.
        Assertions.assertEquals(7, solution.numberOfPointsSortAndMerge(list));
        Assertions.assertEquals(7, solution.numberOfPointsUseSet(list));
    }

    @Test
    void testCase2() {
        PointsThatIntersectWithCars solution = new PointsThatIntersectWithCars();

        List<List<Integer>> list = new ArrayList<>();
        list.add(List.of(1,3));
        list.add(List.of(5,8));
        // Explanation: Points intersecting at least one car are 1, 2, 3, 5, 6, 7, 8.
        // There are a total of 7 points, therefore the answer would be 7.
        Assertions.assertEquals(7, solution.numberOfPointsSortAndMerge(list));
        Assertions.assertEquals(7, solution.numberOfPointsUseSet(list));
    }
}
