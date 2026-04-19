package dev.nklip.javacraft.ewrs.scenarios.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External configuration for the standalone scenario driver.
 * It exists so the module can run against a local EWRS app by default while still being easy to retarget in tests.
 */
@ConfigurationProperties("ewrs.scenarios")
@SuppressWarnings("unused")
public class ScenariosProperties {

    private String targetBaseUrl = "http://localhost:8053";
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(10);
    private Duration projectionTimeout = Duration.ofSeconds(10);
    private Duration pollInterval = Duration.ofMillis(100);

    public String getTargetBaseUrl() {
        return targetBaseUrl;
    }

    public void setTargetBaseUrl(String targetBaseUrl) {
        this.targetBaseUrl = targetBaseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getProjectionTimeout() {
        return projectionTimeout;
    }

    public void setProjectionTimeout(Duration projectionTimeout) {
        this.projectionTimeout = projectionTimeout;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }
}
