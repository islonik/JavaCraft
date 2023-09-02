package my.javacraft.bdd.cucumber.step;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.bdd.service.BMIService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Slf4j
public class BMIStepDefinition {

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

    @When("we use batch to test BMI calculator")
    public void applyPutRequestWithGasReading(DataTable table) {
        List<List<String>> rows = table.cells();
        for (List<String> row : rows) {
            String weight = row.get(0);
            String height = row.get(1);
            boolean isImperial = Boolean.parseBoolean(row.get(2));
            String expected = row.get(3);

            BigDecimal actual = bmiService.calculate(new BigDecimal(weight), new BigDecimal(height), isImperial);

            Assertions.assertEquals(new BigDecimal(expected), actual);
        }
    }


}
