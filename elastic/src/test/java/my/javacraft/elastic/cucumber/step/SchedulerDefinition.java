package my.javacraft.elastic.cucumber.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.cucumber.config.CucumberSpringConfiguration;
import my.javacraft.elastic.model.SeekType;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
import my.javacraft.elastic.service.DateService;
import my.javacraft.elastic.service.SchedulerService;
import my.javacraft.elastic.service.history.UserHistoryIngestionService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class SchedulerDefinition {

    @Autowired
    UserHistoryIngestionService userHistoryIngestionService;
    @Autowired
    DateService dateService;
    @Autowired
    SchedulerService schedulerService;

    @Given("there are {int} outdated records")
    public void createOutdatedRecords(Integer records) throws IOException {
        log.info("creating outdated records...");

        List<UserClickResponse> responses = new ArrayList<>();
        for (int i = 0; i < records; i++) {
            UserClick userClick = new UserClick();
            userClick.setRecordId("document-id-" + i);
            userClick.setSearchType(SeekType.PEOPLE.toString());
            userClick.setUserId("nl8111");
            userClick.setSearchPattern("FIXED INCOME");

            UserClickResponse userClickResponse = userHistoryIngestionService.ingestUserClick(
                    userClick, dateService.getNDaysBeforeDate(200 + i));
            responses.add(userClickResponse);
        }

        log.info("created outdated records = {}", responses.size());

    }

    @Then("execute cleanup job with expected result of {long}")
    public void executeCleanUpJob(Long expectedResult) throws InterruptedException {
        CucumberSpringConfiguration.waitAsElasticSearchIsEventuallyConsistentDB();

        Assertions.assertEquals(expectedResult, schedulerService.removeOldHistoryRecords());
    }
}
