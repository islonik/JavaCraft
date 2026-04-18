package dev.nklip.javacraft.ses.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI sesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SES Work Request Service")
                .description("Event-sourced work request service with CQRS projections, replay, and SSE updates.")
                .version("v1"));
    }
}
