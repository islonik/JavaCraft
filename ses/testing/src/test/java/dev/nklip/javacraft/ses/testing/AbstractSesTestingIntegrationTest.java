package dev.nklip.javacraft.ses.testing;

import dev.nklip.javacraft.ses.app.SesApplication;
import dev.nklip.javacraft.ses.events.EventsMonitor;
import dev.nklip.javacraft.ses.testing.cucumber.config.PostgresContainerInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
        classes = SesApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "logging.level.dev.nklip.javacraft.ses=WARN"
        }
)
@ContextConfiguration(initializers = PostgresContainerInitializer.class)
public abstract class AbstractSesTestingIntegrationTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected EventsMonitor eventsMonitor;

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
