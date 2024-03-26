package my.javacraft.soap2rest.rest.app.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.rest.app.dao.MetricsDao;
import my.javacraft.soap2rest.rest.app.service.SmartService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SmartControllerTest {

    @Mock
    MetricsDao metricsDao;

    @Mock
    SmartService smartService;

    SmartController smartController;

    @BeforeEach
    public void beforeEach() {
        this.smartController = new SmartController(metricsDao, smartService);
        this.smartController.setSmartMessage("Hello World!");
    }

    @Test
    public void testGetDefaultMessage() {
        ResponseEntity<String> response = smartController.getDefault();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Hello World!", response.getBody());
    }

    @Test
    public void testGetMetrics() {
        when(metricsDao.findByAccountId(eq(111L))).thenReturn(createMetrics());

        ResponseEntity<Metrics> response = smartController.getMetrics(111L);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(111L, response.getBody().getAccountId());
    }

    @Test
    public void testGetLatestMetrics() {
        when(metricsDao.findLatestMetrics(eq(111L))).thenReturn(createMetrics());

        ResponseEntity<Metrics> response = smartController.getLatestMetrics(111L);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(111L, response.getBody().getAccountId());
    }

    @Test
    public void testPutMetrics() {
        when(smartService.submit(any())).thenReturn(Boolean.TRUE);

        ResponseEntity<Boolean> response = smartController.putMetrics(createMetrics());

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    public void testDeleteAllMetrics() {
        when(smartService.deleteAll()).thenReturn(Boolean.TRUE);

        ResponseEntity<Boolean> response = smartController.deleteAllMetrics();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(Boolean.TRUE, response.getBody());
    }

    private Metrics createMetrics() {
        Metric electricMetric = new Metric();
        electricMetric.setId(123L);
        electricMetric.setMeterId(1L);
        electricMetric.setReading(new BigDecimal(23));

        List<Metric> electricList = new ArrayList<>();
        electricList.add(electricMetric);

        Metric gasMetric = new Metric();
        gasMetric.setId(124L);
        gasMetric.setMeterId(2L);
        gasMetric.setReading(new BigDecimal(24));

        List<Metric> gasList = new ArrayList<>();
        gasList.add(gasMetric);

        Metrics metrics = new Metrics();
        metrics.setAccountId(111L);
        metrics.setElecReadings(electricList);
        metrics.setElecReadings(gasList);

        return metrics;
    }

}
