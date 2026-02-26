package my.javacraft.bdd.cucumber.conf;

import io.cucumber.spring.CucumberContextConfiguration;
import my.javacraft.bdd.Application;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration {
}
