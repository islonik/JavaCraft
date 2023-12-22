package my.javacraft.elastic.config;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
public class ElasticsearchConfiguration {

    @Value("${spring.elastic.cluster.host:http://localhost}")
    private String host;
    @Value("${spring.elastic.cluster.port:9200}")
    private String port;
    @Value("${spring.elastic.cluster.user:elastic}")
    private String user;
    @Value("${spring.elastic.cluster.pass}")
    private String pass;
    @Value("${spring.elastic.cluster.schema}")
    private String schema;
    @Value("${spring.elastic.cluster.ssl.path}")
    private String sslPath;

    /**
     * I load there the same certificate which Kibana uses to connect to elastic search instance.
     */
    public SSLContext getSslContext() throws Exception {
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
        String serverUrl = host + ":" + port;
        log.info("Creating rest client for elasticsearch cluster (with url = '{}' and schema = '{}')...",
                serverUrl, schema);

        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));

        SSLContext sslContext = getSslContext();

        RestClient restClient = RestClient
                .builder(new HttpHost(host, Integer.parseInt(port), schema))
                .setHttpClientConfigCallback(hccc -> {
                    hccc
                            .disableAuthCaching()
                            .setSSLContext(sslContext)
                            .setDefaultCredentialsProvider(provider);
                    return hccc;
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return objectMapper;
    }

}
