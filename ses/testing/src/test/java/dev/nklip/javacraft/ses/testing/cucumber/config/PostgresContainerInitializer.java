package dev.nklip.javacraft.ses.testing.cucumber.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("deprecation")
public class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> CONTAINER = createContainer();

    static {
        CONTAINER.start();
    }

    @SuppressWarnings("resource")
    private static PostgreSQLContainer<?> createContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse(System.getProperty("tc.image.postgresql", "postgres:17")))
                .withDatabaseName("ses")
                .withUsername("ses")
                .withPassword("ses");
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + CONTAINER.getUsername(),
                "spring.datasource.password=" + CONTAINER.getPassword(),
                "spring.datasource.driver-class-name=" + CONTAINER.getDriverClassName()
        ).applyTo(applicationContext.getEnvironment());
    }
}
