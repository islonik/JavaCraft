package my.javacraft.bdd.cucumber.step;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.bdd.service.BMIService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Slf4j
public class BMIStepDeninition {

    @Autowired
    BMIService bmiService;

    private BigDecimal expectedBmi;

    @When("a person has weight = {string} kg and height = {string} metres")
    public void calculateBmiInMetric(String weight, String height) {
        this.expectedBmi = bmiService.calculate(new BigDecimal(weight), new BigDecimal(height), false);
    }

    @When("a person has weight = {string} lbs and height = {string} inches")
    public void calculateBmiInImperial(String weight, String height) {
        this.expectedBmi = bmiService.calculate(new BigDecimal(weight), new BigDecimal(height), true);
    }

    @Then("that person should have BMI = {string} kg m2")
    @Then("that person should have BMI = {string}")
    public void checkBMI(String actualBmi) {
        Assertions.assertEquals(expectedBmi, new BigDecimal(actualBmi));
    }



}
