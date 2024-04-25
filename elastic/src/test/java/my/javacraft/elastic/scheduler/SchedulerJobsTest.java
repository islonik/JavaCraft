package my.javacraft.elastic.scheduler;

import my.javacraft.elastic.service.SchedulerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SchedulerJobsTest {

    @Test
    public void testCleanUpHistoryTask() {
        SchedulerService schedulerService = Mockito.mock(SchedulerService.class);
        SchedulerJobs schedulerJobs = new SchedulerJobs(schedulerService);

        Mockito.when(schedulerService.removeOldHistoryRecords()).thenReturn(42L);

        schedulerJobs.cleanUpHistoryTask();

        Mockito.verify(
                schedulerService,
                Mockito.atLeast(1)
        ).removeOldHistoryRecords();

    }
}
