package my.javacraft.soap2rest.rest.app.cucumber.conf;

import io.cucumber.spring.CucumberContextConfiguration;
import my.javacraft.soap2rest.rest.app.Application;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CucumberSpringConfiguration {
}
