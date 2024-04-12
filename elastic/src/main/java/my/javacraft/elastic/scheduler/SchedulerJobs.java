package my.javacraft.elastic.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.service.SchedulerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerJobs {

    private final SchedulerService schedulerService;

    /**
     * The first 0 represents the second at which the task will run (0th second).
     * The second 0 represents the minute at which the task will run (0th minute).
     * The * in the other positions represents any value, so it will run every hour regardless of the day of the month, month, day of the week, or year.
     */
    @Scheduled(cron = "0 0 * * * *") // Runs at the top of every hour
    public void cleanUpHistoryTask() {
        log.info("executing clean up history task...");

        log.info("removed documents by clean up history task = '{}'",
                schedulerService.removeOldHistoryRecords()
        );
    }
}
