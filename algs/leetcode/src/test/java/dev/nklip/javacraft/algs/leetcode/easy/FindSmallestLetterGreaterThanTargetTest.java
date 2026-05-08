package dev.nklip.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FindSmallestLetterGreaterThanTargetTest {

    @Test
    void testCase1() {
        FindSmallestLetterGreaterThanTarget solution = new FindSmallestLetterGreaterThanTarget();

        // Explanation: The smallest character that is lexicographically greater than 'a' in letters is 'c'.
        Assertions.assertEquals('c', solution.nextGreatestLetter(new char[]{'c','f','j'}, 'a'));
        // Explanation: The smallest character that is lexicographically greater than 'c' in letters is 'f'.
        Assertions.assertEquals('f', solution.nextGreatestLetter(new char[]{'c','f','j'}, 'c'));
        // Explanation: The smallest character that is lexicographically greater than 'd' in letters is 'f'.
        Assertions.assertEquals('f', solution.nextGreatestLetter(new char[]{'c','f','j'}, 'd'));
    }

    @Test
    void testCase2() {
        FindSmallestLetterGreaterThanTarget solution = new FindSmallestLetterGreaterThanTarget();

        // Explanation: There are no characters in letters that is lexicographically greater than 'z' so we return letters[0].
        Assertions.assertEquals('x', solution.nextGreatestLetter(new char[]{'x','x','y','y'}, 'z'));
        Assertions.assertEquals('x', solution.nextGreatestLetter(new char[]{'x','x','y','y'}, 'y'));
        Assertions.assertEquals('y', solution.nextGreatestLetter(new char[]{'x','x','y','y'}, 'x'));
    }

    @Test
    void testCase3() {
        FindSmallestLetterGreaterThanTarget solution = new FindSmallestLetterGreaterThanTarget();

        Assertions.assertEquals('n', solution.nextGreatestLetter(new char[]{'e','e','e','e','e','e','n','n','n','n'}, 'f'));
    }

    @Test
    void testCase4() {
        FindSmallestLetterGreaterThanTarget solution = new FindSmallestLetterGreaterThanTarget();

        Assertions.assertEquals('r', solution.nextGreatestLetter(new char[]{'a','a','r','r'}, 'o'));
    }
 }
