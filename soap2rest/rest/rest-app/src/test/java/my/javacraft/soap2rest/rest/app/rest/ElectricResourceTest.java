package my.javacraft.soap2rest.rest.app.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.service.ElectricService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

public class ElectricResourceTest {

    ElectricService electricService = mock(ElectricService.class);

    @Test
    public void testGetElectricMetrics() {
        ElectricResource electricResource = new ElectricResource(electricService);

        List<Metric> metricList = new ArrayList<>();
        Metric metric = new Metric();
        metric.setId(123L);
        metric.setMeterId(1L);
        metric.setReading(new BigDecimal(23));

        metricList.add(metric);
        when(electricService.getMetricsByAccountId(anyLong())).thenReturn(metricList);

        ResponseEntity<List<Metric>> response = electricResource.getElectricMetrics(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(1, response.getBody().size());
    }
}
