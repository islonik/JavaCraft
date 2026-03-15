package my.javacraft.elastic.cucumber.conf;

import io.cucumber.spring.CucumberContextConfiguration;
import java.util.function.Supplier;
import my.javacraft.elastic.Application;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@CucumberContextConfiguration
@SpringBootTest(
        classes = Application.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
public class CucumberSpringConfiguration {

    private static final int POLL_INTERVAL_MILLIS = 200;
    private static final int MAX_WAIT_MILLIS = 2000;

    public static void waitAsElasticSearchIsEventuallyConsistentDB() throws InterruptedException {
        Thread.sleep(2000);
    }

    public static boolean assertWithWait(long expected, Supplier<Long> supplier) throws InterruptedException {
        boolean confirmed = false;

        int elapsedMillis = 0;
        while (elapsedMillis < MAX_WAIT_MILLIS) {

            if (expected == supplier.get()) {
                confirmed = true;
                break;
            }
            Thread.sleep(POLL_INTERVAL_MILLIS);

            elapsedMillis += POLL_INTERVAL_MILLIS;
        }
        return confirmed;
    }

}
