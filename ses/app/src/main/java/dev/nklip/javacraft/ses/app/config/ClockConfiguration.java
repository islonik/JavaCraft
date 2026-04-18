package dev.nklip.javacraft.ses.app.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfiguration {

    @Bean
    public Clock sesClock() {
        return Clock.systemUTC();
    }
}
