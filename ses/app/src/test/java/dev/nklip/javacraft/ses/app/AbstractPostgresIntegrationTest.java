package dev.nklip.javacraft.ses.app;

import dev.nklip.javacraft.ses.events.EventsMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        classes = SesApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "ses.projector.listener.enabled=false",
                "logging.level.dev.nklip.javacraft.ses=WARN"
        }
)
@SuppressWarnings("deprecation")
public abstract class AbstractPostgresIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse(System.getProperty("tc.image.postgresql", "postgres:17"))
    )
            .withDatabaseName("ses")
            .withUsername("ses")
            .withPassword("ses");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected EventsMonitor eventsMonitor;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @BeforeEach
    void resetDatabaseState() {
        jdbcTemplate.execute("truncate table work_request_projection, event_store restart identity cascade");
        jdbcTemplate.execute("alter sequence work_request_id_seq restart with 1000");
        jdbcTemplate.execute("update projection_checkpoint set last_event_store_id = 0 where projection_name = 'ses-projector'");
        jdbcTemplate.execute("delete from budget_projection");
        jdbcTemplate.execute("""
                insert into budget_projection (budget_code, initial_budget, reserved_amount, remaining_budget, last_updated_at)
                select budget_code, initial_budget, 0, initial_budget, current_timestamp
                from budget_reference
                order by budget_code
                """);
        eventsMonitor.clear();
    }
}
