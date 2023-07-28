package my.javacraft.soap2rest.rest.app.service;

import java.math.BigDecimal;
import java.sql.Date;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricValidationServiceTest {

    @Test
    public void testValidate() {
        GasMetric extracted = new GasMetric();
        extracted.setId(1L);
        extracted.setMeterId(100L);
        extracted.setReading(new BigDecimal("2556.789"));
        extracted.setDate(Date.valueOf("2023-07-15"));

        GasMetric submitted = new GasMetric();
        submitted.setId(1L);
        submitted.setMeterId(100L);
        submitted.setReading(new BigDecimal("1556.789"));
        submitted.setDate(Date.valueOf("2023-07-14"));

        MetricValidationService metricValidationService = new MetricValidationService();

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            metricValidationService.validate(extracted, submitted);
        });
        Assertions.assertEquals("New metrics should be higher than the previous read.", exception.getMessage());

        // new reading
        submitted.setReading(new BigDecimal("2800.789"));

        exception = Assertions.assertThrows(RuntimeException.class, () -> {
            metricValidationService.validate(extracted, submitted);
        });
        Assertions.assertEquals("You are trying to submit for a past date.", exception.getMessage());

        // new date
        submitted.setDate(Date.valueOf("2023-07-15"));

        exception = Assertions.assertThrows(RuntimeException.class, () -> {
            metricValidationService.validate(extracted, submitted);
        });
        Assertions.assertEquals("You already submitted your metrics for today.", exception.getMessage());
    }
}
