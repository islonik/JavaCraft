package dev.nklip.javacraft.ewrs.testing.cucumber.config;

import dev.nklip.javacraft.ewrs.testing.EwrsTestingApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(
        classes = EwrsTestingApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.dev.nklip.javacraft.ewrs=WARN",
                "spring.application.name=ewrs-testing",
                "spring.http.client.factory=simple",
                "ewrs.scenarios.target-base-url=",
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false"
        }
)
@ContextConfiguration(initializers = PostgresContainerInitializer.class)
@SuppressWarnings("unused")
public class CucumberSpringConfiguration {
}
