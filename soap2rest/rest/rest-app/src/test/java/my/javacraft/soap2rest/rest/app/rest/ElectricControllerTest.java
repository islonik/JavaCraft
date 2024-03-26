package my.javacraft.soap2rest.rest.app.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.service.ElectricService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElectricControllerTest {

    @Mock
    ElectricService electricService;

    @Test
    public void testGetElectricMetrics() {
        ElectricController electricController = new ElectricController(electricService);

        when(electricService.getMetricsByAccountId(anyLong())).thenReturn(createMetricList());

        ResponseEntity<List<Metric>> response = electricController.getElectricMetrics(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(1, response.getBody().size());
        verify(electricService, atLeastOnce()).getMetricsByAccountId(anyLong());
    }

    @Test
    public void testGetLatestElectricMetric() {
        ElectricController electricController = new ElectricController(electricService);

        when(electricService.findLatestMetric(anyLong())).thenReturn(createMetricList().getFirst());

        ResponseEntity<Metric> response = electricController.getLatestElectricMetric(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(electricService, atLeastOnce()).findLatestMetric(anyLong());
    }

    @Test
    public void testPutNewElectricMetric() {
        ElectricController electricController = new ElectricController(electricService);

        Metric metric = createMetricList().getFirst();
        when(electricService.submit(any())).thenReturn(metric);

        ResponseEntity<Metric> response = electricController.putNewElectricMetric(metric);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(electricService, atLeastOnce()).submit(any());
    }

    @Test
    public void testDeleteAllElectricMetrics() {
        ElectricController electricController = new ElectricController(electricService);

        when(electricService.deleteAll()).thenReturn(Boolean.TRUE);

        ResponseEntity<Boolean> response = electricController.deleteAllElectricMetrics();
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(electricService, atLeastOnce()).deleteAll();
    }

    private List<Metric> createMetricList() {
        Metric metric = new Metric();
        metric.setId(123L);
        metric.setMeterId(1L);
        metric.setReading(new BigDecimal(23));

        List<Metric> metricList = new ArrayList<>();
        metricList.add(metric);
        return metricList;
    }
}
