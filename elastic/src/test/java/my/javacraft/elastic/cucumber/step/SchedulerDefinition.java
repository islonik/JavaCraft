package my.javacraft.elastic.cucumber.step;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.cucumber.config.CucumberSpringConfiguration;
import my.javacraft.elastic.model.Client;
import my.javacraft.elastic.model.SeekType;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
import my.javacraft.elastic.service.DateService;
import my.javacraft.elastic.service.SchedulerService;
import my.javacraft.elastic.service.UserHistoryService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class SchedulerDefinition {

    @Autowired
    UserHistoryService userHistoryService;
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
            userClick.setDocumentId("document-id-" + i);
            userClick.setSearchType(SeekType.PEOPLE.toString());
            userClick.setUserId("nl8111");
            userClick.setSearchPattern("FIXED INCOME");

            UserClickResponse userClickResponse = userHistoryService.capture(userClick, dateService.getNDaysBeforeDate(91 + i));
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
