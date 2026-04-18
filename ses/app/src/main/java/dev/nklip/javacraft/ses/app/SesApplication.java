package dev.nklip.javacraft.ses.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.nklip.javacraft.ses")
public class SesApplication {

    static void main(String[] args) {
        SpringApplication.run(SesApplication.class, args);
    }
}
