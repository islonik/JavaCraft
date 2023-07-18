package my.javacraft.bdd.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Body mass index (BMI) is a measurement of a person's weight in relation to their height.
 *
 * It offers an inexpensive and simple method of categorising people according to their BMI value
 * so that we can screen people’s weight category and indicate their potential risk for health conditions.
 */
@Service
public class BMIService {

    /**
     * BMI = Weight (kg) / Height (m)²
     *
     * BMI = [Weight (lbs) / Height (inches)²] x 703
     *
     * The imperial BMI formula is your weight in pounds (lbs) divided by your height in inches,
     * squared and then you multiply this figure by a conversion factor of 703.
     */
    public BigDecimal calculate(BigDecimal weight, BigDecimal height, boolean isImperial) {
        if (isImperial) {
            BigDecimal result = weight.divide(height.multiply(height), 1000, RoundingMode.HALF_EVEN);
            return result.multiply(BigDecimal.valueOf(703)).setScale(2, RoundingMode.HALF_EVEN);
        } else {
            return weight.divide(height.multiply(height), 2, RoundingMode.HALF_EVEN);
        }
    }


    /**
     * Underweight < 18.50
     * Severe thinness < 16.00
     * Moderate thinness 16.00 - 16.99
     * Mild thinness 17.00 - 18.49
     * Normal range 18.50 - 24.99
     * Pre-obese 25.00 - 29.99
     * Obese ≥ 30.00
     * Obese class I 30.00 - 34.99
     * Obese class II 35.00 - 39.99
     * Obese class III ≥40.00
     */
    public String bmi2category(BigDecimal bmi) {
        if (bmi.compareTo(BigDecimal.valueOf(16.00)) < 0) { // "<"
            return "Severe thinness as your BMI is less than 16.00";
        } else if (bmi.compareTo(BigDecimal.valueOf(16.99)) < 0) {
            return "Moderate thinness as your BMI is less than 16.99";
        } else if (bmi.compareTo(BigDecimal.valueOf(18.49)) < 0) {
            return "Mild thinness as your BMI is less than 18.49";
        } else if (bmi.compareTo(BigDecimal.valueOf(24.99)) < 0) {
            return "Normal range as your BMI is less than 24.99";
        } else if (bmi.compareTo(BigDecimal.valueOf(29.99)) < 0) {
            return "Pre-obese as your BMI is less than 29.99";
        } else if (bmi.compareTo(BigDecimal.valueOf(34.99)) < 0) {
            return "Obese class I as your BMI is less than 34.99";
        } else if (bmi.compareTo(BigDecimal.valueOf(39.99)) < 0) {
            return "Obese class II as your BMI is less than 39.99";
        } else {
            return "Obese class III as your BMI is over 40";
        }
    }
}
