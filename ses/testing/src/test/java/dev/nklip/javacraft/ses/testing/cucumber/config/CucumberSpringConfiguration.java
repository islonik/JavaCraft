package dev.nklip.javacraft.ses.testing.cucumber.config;

import dev.nklip.javacraft.ses.app.SesApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(
        classes = SesApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.dev.nklip.javacraft.ses=WARN"
        }
)
@ContextConfiguration(initializers = PostgresContainerInitializer.class)
@SuppressWarnings("unused")
public class CucumberSpringConfiguration {
}
