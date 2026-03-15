package my.javacraft.elastic.cucumber.conf;

import io.cucumber.spring.CucumberContextConfiguration;
import my.javacraft.elastic.Application;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@CucumberContextConfiguration
@SpringBootTest(
        classes = Application.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
public class CucumberSpringConfiguration {

    public static void waitAsElasticSearchIsEventuallyConsistentDB() throws InterruptedException {
        Thread.sleep(2000);
    }
}
