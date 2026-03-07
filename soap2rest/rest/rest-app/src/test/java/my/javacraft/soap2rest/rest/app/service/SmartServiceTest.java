package my.javacraft.soap2rest.rest.app.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SmartServiceTest {

    @Mock
    GasService gasService;

    @Mock
    ElectricService electricService;

    @Test
    public void testDeleteAllByAccountId() {
        SmartService smartService = new SmartService(gasService, electricService);
        when(gasService.deleteAllByAccountId(1L)).thenReturn(2);
        when(electricService.deleteAllByAccountId(1L)).thenReturn(3);

        int deleted = smartService.deleteAllByAccountId(1L);

        Assertions.assertEquals(5, deleted);
        verify(gasService).deleteAllByAccountId(1L);
        verify(electricService).deleteAllByAccountId(1L);
    }

    @Test
    public void testDeleteAllByAccountIdWhenNoMetricsFound() {
        SmartService smartService = new SmartService(gasService, electricService);
        when(gasService.deleteAllByAccountId(7L)).thenReturn(0);
        when(electricService.deleteAllByAccountId(7L)).thenReturn(0);

        int deleted = smartService.deleteAllByAccountId(7L);

        Assertions.assertEquals(0, deleted);
        verify(gasService).deleteAllByAccountId(7L);
        verify(electricService).deleteAllByAccountId(7L);
    }
}
