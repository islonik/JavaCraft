package my.javacraft.elastic.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ElasticsearchConfiguration {

    @Value("${spring.elastic.cluster.host:http://localhost}")
    private String host;
    @Value("${spring.elastic.cluster.port:9200}")
    private String port;
    @Value("${spring.elastic.cluster.user:elastic")
    private String user;
    @Value("${spring.elastic.cluster.pass}")
    private String pass;
    @Value("${spring.elastic.cluster.schema}")
    private String schema;

    @Bean
    public ElasticsearchClient getElasticsearchClient() {
        String serverUrl = host + ":" + port;
        log.info("Creating rest client for elasticsearch cluster (with url = '{}' and schema = '{}')...",
                serverUrl, schema);
        RestClient restClient = RestClient
                .builder(new HttpHost(host, Integer.parseInt(port), schema))
                .setDefaultHeaders(new Header[] {
                        new BasicHeader("Authorization", "ApiKey " + pass)
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }


}
