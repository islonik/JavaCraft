package my.javacraft.elastic.cucumber.config;


import io.cucumber.spring.CucumberContextConfiguration;
import my.javacraft.elastic.Application;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@CucumberContextConfiguration
@SpringBootTest(
        classes = Application.class,
        webEnvironment = WebEnvironment.DEFINED_PORT
)
public class CucumberSpringConfiguration {
}
