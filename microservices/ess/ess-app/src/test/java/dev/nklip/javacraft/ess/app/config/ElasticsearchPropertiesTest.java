package dev.nklip.javacraft.ess.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(ElasticsearchProperties.class)
@TestPropertySource(properties = {
        "spring.elastic.cluster.host=es-node",
        "spring.elastic.cluster.port=9300",
        "spring.elastic.cluster.user=reader",
        "spring.elastic.cluster.pass=secret",
        "spring.elastic.cluster.ssl.enabled=false",
        "spring.elastic.cluster.ssl.path=cert/test-http_ca.crt",
        "spring.elastic.cluster.timeout.connect-ms=1234",
        "spring.elastic.cluster.timeout.socket-ms=5678",
        "spring.elastic.cluster.timeout.request-ms=4321",
        "spring.elastic.cluster.retry.max-attempts=7",
        "spring.elastic.cluster.retry.initial-backoff-ms=345",
        "spring.elastic.cluster.retry.max-backoff-ms=6789"
})
class ElasticsearchPropertiesTest {

    @Autowired
    ElasticsearchProperties properties;

    @Test
    void testElasticsearchPropertiesBindFromProperties() {
        assertEquals("es-node", properties.host());
        assertEquals(9300, properties.port());
        assertEquals("reader", properties.user());
        assertEquals("secret", properties.pass());
        assertFalse(properties.ssl().enabled());
        assertEquals("cert/test-http_ca.crt", properties.ssl().path());
        assertEquals(1234, properties.timeout().connectMs());
        assertEquals(5678, properties.timeout().socketMs());
        assertEquals(4321, properties.timeout().requestMs());
        assertEquals(7, properties.retry().maxAttempts());
        assertEquals(345L, properties.retry().initialBackoffMs());
        assertEquals(6789L, properties.retry().maxBackoffMs());
    }
}
