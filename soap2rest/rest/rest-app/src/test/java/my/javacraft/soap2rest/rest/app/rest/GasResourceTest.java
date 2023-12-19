package my.javacraft.soap2rest.rest.app.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.service.GasService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
public class GasResourceTest {

    @Mock
    GasService gasService;

    @Test
    public void testGetElectricMetrics() {
        GasResource gasResource = new GasResource(gasService);

        when(gasService.getMetricsByAccountId(anyLong())).thenReturn(createMetricList());

        ResponseEntity<List<Metric>> response = gasResource.getGasMetrics(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(1, response.getBody().size());
        verify(gasService, atLeastOnce()).getMetricsByAccountId(anyLong());
    }

    @Test
    public void testGetLatestElectricMetric() {
        GasResource gasResource = new GasResource(gasService);

        when(gasService.findLatestMetric(anyLong())).thenReturn(createMetricList().get(0));

        ResponseEntity<Metric> response = gasResource.getLatestGasMetric(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(gasService, atLeastOnce()).findLatestMetric(anyLong());
    }

    @Test
    public void testPutNewElectricMetric() {
        GasResource gasResource = new GasResource(gasService);

        Metric metric = createMetricList().get(0);
        when(gasService.submit(any())).thenReturn(metric);

        ResponseEntity<Metric> response = gasResource.putNewGasMetric(metric);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(gasService, atLeastOnce()).submit(any());
    }

    @Test
    public void testDeleteAllElectricMetrics() {
        GasResource gasResource = new GasResource(gasService);

        when(gasService.deleteAll()).thenReturn(Boolean.TRUE);

        ResponseEntity<Boolean> response = gasResource.deleteAllGasMetrics();
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        verify(gasService, atLeastOnce()).deleteAll();
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
