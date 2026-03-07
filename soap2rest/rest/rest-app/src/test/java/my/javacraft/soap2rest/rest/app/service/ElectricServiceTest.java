package my.javacraft.soap2rest.rest.app.service;

import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElectricServiceTest {

    @Mock
    MetricService metricService;

    @Mock
    MetricValidationService metricValidationService;

    @Mock
    ElectricMetricDao electricMetricDao;

    @Test
    public void testDeleteAllByAccountId() {
        ElectricService electricService = new ElectricService(
                metricService,
                metricValidationService,
                electricMetricDao
        );
        when(electricMetricDao.deleteByAccountId(1L)).thenReturn(2);

        int deleted = electricService.deleteAllByAccountId(1L);

        Assertions.assertEquals(2, deleted);
        verify(electricMetricDao).deleteByAccountId(1L);
    }

    @Test
    public void testDeleteAllByAccountIdWhenNoMetricsFound() {
        ElectricService electricService = new ElectricService(
                metricService,
                metricValidationService,
                electricMetricDao
        );
        when(electricMetricDao.deleteByAccountId(7L)).thenReturn(0);

        int deleted = electricService.deleteAllByAccountId(7L);

        Assertions.assertEquals(0, deleted);
        verify(electricMetricDao).deleteByAccountId(7L);
    }
}
