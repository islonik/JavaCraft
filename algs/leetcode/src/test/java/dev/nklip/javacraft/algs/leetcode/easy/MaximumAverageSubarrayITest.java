package dev.nklip.javacraft.algs.leetcode.easy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MaximumAverageSubarrayITest {

    @Test
    void testCase1() {
        MaximumAverageSubarrayI solution = new MaximumAverageSubarrayI();

        // Explanation: Maximum average is (12 - 5 - 6 + 50) / 4 = 51 / 4 = 12.75
        Assertions.assertEquals(12.75000, solution.findMaxAverage(new int[]{1,12,-5,-6,50,3}, 4));
    }

    @Test
    void testCase2() {
        MaximumAverageSubarrayI solution = new MaximumAverageSubarrayI();

        Assertions.assertEquals(5.00000, solution.findMaxAverage(new int[]{5}, 1));
    }

    @Test
    void testCase3() {
        MaximumAverageSubarrayI solution = new MaximumAverageSubarrayI();

        Assertions.assertEquals(-1.00000, solution.findMaxAverage(new int[]{-1}, 1));
    }

    @Test
    void testCase4() {
        MaximumAverageSubarrayI solution = new MaximumAverageSubarrayI();

        double maxAverage = solution.findMaxAverage(new int[]{
                -6662, 5432,-8558,-8935, 8731,-3083, 4115, 9931,-4006,-3284,
                -3024, 1714,-2825,-2374,-2750,-959,  6516, 9356, 8040,-2169,
                -9490,-3068, 6299, 7823,-9767, 5751,-7897, 6680,-1293,-3486,
                -6785, 6337,-9158,-4183, 6240,-2846,-2588,-5458,-9576,-1501,
                -908, -5477, 7596,-8863,-4088, 7922, 8231,-4928, 7636,-3994,
                -243, -1327, 8425,-3468,-4218,-364,  4257, 5690, 1035, 6217,
                8880,  4127,-6299,-1831, 2854,-4498,-6983, -677, 2216,-1938,
                3348,  4099, 3591, 9076,  942, 4571,-4200, 7271,-6920,-1886,
                662,   7844, 3658,-6562,-2106, -296,-3280, 8909,-8352,-9413,
                3513,  1352,-8825
        }, 90);

        Assertions.assertEquals(37.25556, new BigDecimal(maxAverage)
                .setScale(5, RoundingMode.HALF_UP)
                .doubleValue());
    }

    @Test
    void testCase5() {
        MaximumAverageSubarrayI solution = new MaximumAverageSubarrayI();

        double maxAverage = solution.findMaxAverage(new int[]{
                8860,  -853, 6534, 4477,-4589, 8646,-6155,-5577,-1656,-5779,
                -2619,-8604,-1358,-8009, 4983, 7063, 3104,-1560, 4080, 2763,
                5616, -2375, 2848, 1394,-7173,-5225,-8244,-809,  8025,-4072,
                -4391,-9579, 1407, 6700, 2421,-6685, 5481,-1732,-8892,-6645,
                3077,  3287,-4149, 8701,-4393,-9070,-1777, 2237,-3253, -506,
                -4931,-7366,-8132, 5406,-6300, -275,-1908,   67, 3569, 1433,
                -7262, -437, 8303, 4498, -379, 3054,-6285, 4203, 6908, 4433,
                3077, 2288, 9733,-8067, 3007, 9725, 9669, 1362,-2561,-4225,
                5442,-9006, -429,  160,-9234,-4444, 3586,-5711,-9506,  -79,
                -4418,-4348,-5891
        }, 93);

        Assertions.assertEquals(-594.58065, new BigDecimal(maxAverage)
                .setScale(5, RoundingMode.HALF_UP)
                .doubleValue());
    }
}
