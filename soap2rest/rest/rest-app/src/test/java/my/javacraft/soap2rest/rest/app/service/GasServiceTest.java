package my.javacraft.soap2rest.rest.app.service;

import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GasServiceTest {

    @Mock
    MetricService metricService;

    @Mock
    MetricValidationService metricValidationService;

    @Mock
    GasMetricDao gasMetricDao;

    @Test
    public void testDeleteAllByAccountId() {
        GasService gasService = new GasService(
                metricService,
                metricValidationService,
                gasMetricDao
        );
        when(gasMetricDao.deleteByAccountId(1L)).thenReturn(2);

        int deleted = gasService.deleteAllByAccountId(1L);

        Assertions.assertEquals(2, deleted);
        verify(gasMetricDao).deleteByAccountId(1L);
    }

    @Test
    public void testDeleteAllByAccountIdWhenNoMetricsFound() {
        GasService gasService = new GasService(
                metricService,
                metricValidationService,
                gasMetricDao
        );
        when(gasMetricDao.deleteByAccountId(7L)).thenReturn(0);

        int deleted = gasService.deleteAllByAccountId(7L);

        Assertions.assertEquals(0, deleted);
        verify(gasMetricDao).deleteByAccountId(7L);
    }
}
