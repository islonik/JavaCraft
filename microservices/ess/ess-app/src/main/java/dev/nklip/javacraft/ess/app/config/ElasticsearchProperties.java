package dev.nklip.javacraft.ess.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * External configuration for the Elasticsearch HTTP client and retry transport.
 * Values live in application.yaml under the {@code spring.elastic.cluster:} key.
 */
@ConfigurationProperties(prefix = "spring.elastic.cluster")
public record ElasticsearchProperties(
        @DefaultValue("localhost") String host,
        @DefaultValue("9200") int port,
        @DefaultValue("elastic") String user,
        @DefaultValue("elastic") String pass,
        @DefaultValue SslProperties ssl,
        @DefaultValue TimeoutProperties timeout,
        @DefaultValue RetryProperties retry
) {
    public record SslProperties(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("cert/http_ca.crt") String path
    ) {}

    public record TimeoutProperties(
            @DefaultValue("3000") int connectMs,
            @DefaultValue("10000") int socketMs,
            @DefaultValue("2000") int requestMs
    ) {}

    public record RetryProperties(
            @DefaultValue("3") int maxAttempts,
            @DefaultValue("200") long initialBackoffMs,
            @DefaultValue("2000") long maxBackoffMs
    ) {}
}
