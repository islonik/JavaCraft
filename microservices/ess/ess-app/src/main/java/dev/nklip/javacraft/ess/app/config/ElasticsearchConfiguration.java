package dev.nklip.javacraft.ess.app.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.nklip.javacraft.ess.api.validation.PositiveNumber;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfiguration {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private final ElasticsearchProperties properties;

    /**
     * I load there the same certificate which Kibana uses to connect to elastic search instance.
     */
    SSLContext getSslContext(String sslPath) throws Exception {
        Certificate trustedCa;
        ClassPathResource trustResource = new ClassPathResource(sslPath);
        try (InputStream is = trustResource.getInputStream()) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            trustedCa = factory.generateCertificate(is);
        }
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);
        return SSLContexts
                .custom()
                .loadTrustMaterial(trustStore, null)
                .build();
    }

    @Bean
    public ElasticsearchClient getElasticsearchClient() throws Exception {
        boolean useSsl = properties.ssl().enabled();
        String resolvedSchema = resolveSchema(useSsl);
        int resolvedConnectTimeout = PositiveNumber.positiveOrDefault(
                properties.timeout().connectMs(),
                RestClientBuilder.DEFAULT_CONNECT_TIMEOUT_MILLIS
        );
        int resolvedSocketTimeout = PositiveNumber.positiveOrDefault(
                properties.timeout().socketMs(),
                RestClientBuilder.DEFAULT_SOCKET_TIMEOUT_MILLIS
        );
        int resolvedRequestTimeout = PositiveNumber.positiveOrDefault(properties.timeout().requestMs(), 1000);
        int resolvedRetryAttempts = PositiveNumber.positiveOrDefault(properties.retry().maxAttempts(), 1);
        long resolvedInitialBackoff = PositiveNumber.positiveOrDefault(properties.retry().initialBackoffMs(), 200L);
        long resolvedMaxBackoff = Math.max(
                resolvedInitialBackoff,
                PositiveNumber.positiveOrDefault(properties.retry().maxBackoffMs(), 2_000L)
        );
        String serverUrl = properties.host() + ":" + properties.port();
        log.info(
                "Creating rest client for elasticsearch cluster (url='{}', schema='{}', ssl.enabled='{}', connectTimeoutMs='{}', socketTimeoutMs='{}', requestTimeoutMs='{}', retryAttempts='{}', retryInitialBackoffMs='{}', retryMaxBackoffMs='{}')...",
                serverUrl,
                resolvedSchema,
                useSsl,
                resolvedConnectTimeout,
                resolvedSocketTimeout,
                resolvedRequestTimeout,
                resolvedRetryAttempts,
                resolvedInitialBackoff,
                resolvedMaxBackoff
        );

        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(properties.user(), properties.pass())
        );

        SSLContext sslContext = useSsl ? getSslContext(properties.ssl().path()) : null;
        RestClient restClient = RestClient
                .builder(new HttpHost(properties.host(), properties.port(), resolvedSchema))
                .setRequestConfigCallback(config -> config
                        .setConnectTimeout(resolvedConnectTimeout)
                        .setSocketTimeout(resolvedSocketTimeout)
                        .setConnectionRequestTimeout(resolvedRequestTimeout)
                )
                .setHttpClientConfigCallback(hccc -> {
                    hccc.disableAuthCaching()
                            .setDefaultCredentialsProvider(provider);
                    if (sslContext != null) {
                        hccc.setSSLContext(sslContext);
                    }
                    return hccc;
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RetryingElasticsearchTransport(
                new RestClientTransport(restClient, new JacksonJsonpMapper()),
                resolvedRetryAttempts,
                resolvedInitialBackoff,
                resolvedMaxBackoff
        );

        // And create the API client
        return new ElasticsearchClient(transport);
    }

    static String resolveSchema(boolean useSsl) {
        return useSsl ? HTTPS : HTTP;
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        return objectMapper;
    }

}
