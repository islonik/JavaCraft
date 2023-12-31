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
public class ElectricResourceTest {

    @Mock
    ElectricService electricService;

    @Test
    public void testGetElectricMetrics() {
        ElectricResource electricResource = new ElectricResource(electricService);

        when(electricService.getMetricsByAccountId(anyLong())).thenReturn(createMetricList());

        ResponseEntity<List<Metric>> response = electricResource.getElectricMetrics(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(1, response.getBody().size());
        verify(electricService, atLeastOnce()).getMetricsByAccountId(anyLong());
    }

    @Test
    public void testGetLatestElectricMetric() {
        ElectricResource electricResource = new ElectricResource(electricService);

        when(electricService.findLatestMetric(anyLong())).thenReturn(createMetricList().get(0));

        ResponseEntity<Metric> response = electricResource.getLatestElectricMetric(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(electricService, atLeastOnce()).findLatestMetric(anyLong());
    }

    @Test
    public void testPutNewElectricMetric() {
        ElectricResource electricResource = new ElectricResource(electricService);

        Metric metric = createMetricList().get(0);
        when(electricService.submit(any())).thenReturn(metric);

        ResponseEntity<Metric> response = electricResource.putNewElectricMetric(metric);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(electricService, atLeastOnce()).submit(any());
    }

    @Test
    public void testDeleteAllElectricMetrics() {
        ElectricResource electricResource = new ElectricResource(electricService);

        when(electricService.deleteAll()).thenReturn(Boolean.TRUE);

        ResponseEntity<Boolean> response = electricResource.deleteAllElectricMetrics();
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
