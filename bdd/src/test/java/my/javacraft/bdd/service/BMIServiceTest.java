package my.javacraft.bdd.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BMIServiceTest {

    private final BMIService bmiService = new BMIService();

    @Test
    void testCalculateThrowsExceptionWhenWeightIsNull() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.calculate(null, BigDecimal.ONE, false)
        );
        Assertions.assertEquals("Weight and height must not be null", ex.getMessage());
    }

    @Test
    void testCalculateThrowsExceptionWhenHeightIsNull() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.calculate(BigDecimal.ONE, null, false)
        );
        Assertions.assertEquals("Weight and height must not be null", ex.getMessage());
    }

    @Test
    void testCalculateThrowsExceptionWhenBothAreNull() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.calculate(null, null, true)
        );
        Assertions.assertEquals("Weight and height must not be null", ex.getMessage());
    }

    @Test
    void testCalculateThrowsExceptionWhenWeightIsZero() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.calculate(BigDecimal.ZERO, BigDecimal.ONE, false)
        );
        Assertions.assertEquals("Weight must be positive", ex.getMessage());
    }

    @Test
    void testCalculateThrowsExceptionWhenWeightIsNegative() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.calculate(BigDecimal.valueOf(-70), BigDecimal.valueOf(1.75), false)
        );
        Assertions.assertEquals("Weight must be positive", ex.getMessage());
    }

    @Test
    void testCalculateThrowsExceptionWhenHeightIsZero() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.calculate(BigDecimal.valueOf(75), BigDecimal.ZERO, false)
        );
        Assertions.assertEquals("Height must be positive", ex.getMessage());
    }

    @Test
    void testCalculateThrowsExceptionWhenHeightIsNegative() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.calculate(BigDecimal.valueOf(75), BigDecimal.valueOf(-1.75), true)
        );
        Assertions.assertEquals("Height must be positive", ex.getMessage());
    }

    @Test
    void testCalculateMetricBmi() {
        BigDecimal result = bmiService.calculate(
                BigDecimal.valueOf(75), BigDecimal.valueOf(1.75), false);
        Assertions.assertEquals(new BigDecimal("24.49"), result);
    }

    @Test
    void testCalculateImperialBmi() {
        BigDecimal result = bmiService.calculate(
                BigDecimal.valueOf(150), BigDecimal.valueOf(65), true);
        Assertions.assertEquals(new BigDecimal("24.96"), result);
    }

    @Test
    void testBmi2categoryThrowsExceptionWhenBmiIsNull() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.bmi2category(null)
        );
        Assertions.assertEquals("BMI must not be null", ex.getMessage());
    }

    @Test
    void testBmi2categoryThrowsExceptionWhenBmiIsZero() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.bmi2category(BigDecimal.ZERO)
        );
        Assertions.assertEquals("BMI must be positive", ex.getMessage());
    }

    @Test
    void testBmi2categoryThrowsExceptionWhenBmiIsNegative() {
        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bmiService.bmi2category(BigDecimal.valueOf(-1))
        );
        Assertions.assertEquals("BMI must be positive", ex.getMessage());
    }

}
