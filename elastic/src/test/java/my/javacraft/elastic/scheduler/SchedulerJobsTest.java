package my.javacraft.elastic.scheduler;

import my.javacraft.elastic.service.SchedulerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SchedulerJobsTest {

    @Test
    public void testCleanUpActivityTask() {
        SchedulerService schedulerService = Mockito.mock(SchedulerService.class);
        SchedulerJobs schedulerJobs = new SchedulerJobs(schedulerService);

        Mockito.when(schedulerService.removeOldActivityRecords()).thenReturn(42L);

        schedulerJobs.cleanUpActivityTask();

        Mockito.verify(
                schedulerService,
                Mockito.atLeast(1)
        ).removeOldActivityRecords();

    }
}
