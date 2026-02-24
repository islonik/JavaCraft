package my.javacraft.algs.leetcode.easy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidParenthesesTest {

    @Test
    void testIsValidSimpleCase01() {
        ValidParentheses solution = new ValidParentheses();

        Assertions.assertEquals(true, solution.isValid("()"));
    }

    @Test
    void testIsValidSimpleCase02() {
        ValidParentheses solution = new ValidParentheses();

        Assertions.assertEquals(true, solution.isValid("()[]{}"));
    }

    @Test
    void testIsValidSimpleCase03() {
        ValidParentheses solution = new ValidParentheses();

        Assertions.assertEquals(true, solution.isValid("([])"));
    }

    @Test
    void testIsValidNegativeCase01() {
        ValidParentheses solution = new ValidParentheses();

        Assertions.assertEquals(false, solution.isValid("(]"));
    }

    @Test
    void testIsValidNegativeCase02() {
        ValidParentheses solution = new ValidParentheses();

        Assertions.assertEquals(false, solution.isValid("([)]"));
    }
}
