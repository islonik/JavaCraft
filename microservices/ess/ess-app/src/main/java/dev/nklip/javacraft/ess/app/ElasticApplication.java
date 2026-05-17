package dev.nklip.javacraft.ess.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import dev.nklip.javacraft.ess.app.config.ElasticsearchProperties;
import dev.nklip.javacraft.ess.app.config.SearchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({
        SearchProperties.class,
        ElasticsearchProperties.class
})
@OpenAPIDefinition(
        info = @Info(
                title = "Elastic application",
                version = "1.0",
                description = "Swagger UI for Elastic application"
        )
)
public class ElasticApplication {

    static void main(String[] args){
        log.info("Elastic ElasticApplication starting...");
        SpringApplication.run(ElasticApplication.class, args);
    }

}
