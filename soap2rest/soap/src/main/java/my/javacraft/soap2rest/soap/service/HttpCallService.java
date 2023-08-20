package my.javacraft.soap2rest.soap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.javacraft.soap2rest.rest.api.Metric;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpCallService {

    public static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";

    @Value("${rest-app.host}")
    String host;

    @Value("${rest-app.port}")
    String port;

    public String baseHost() {
        return host + ":" + port;
    }

    MultiValueMap<String, String> getHeaders() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");
        return headers;
    }

    public ResponseEntity<Metric> put(String methodUrl, Metric metric) throws JsonProcessingException {
        MultiValueMap<String, String> headers = getHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(metric), headers);

        RestTemplate restTemplate = new RestTemplate();

        // like that http://localhost:8081/api/v1/smart/1/gas
        String url = "%s%s".formatted(baseHost(), methodUrl);
        return restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                Metric.class
        );
    }

    public ResponseEntity<Boolean> delete(String methodUrl) {
        MultiValueMap<String, String> headers = getHeaders();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        RestTemplate restTemplate = new RestTemplate();

        // like that http://localhost:8081/api/v1/smart/1/gas
        String url = "%s%s".formatted(baseHost(), methodUrl);
        return restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                Boolean.class
        );
    }

    public<T> ResponseEntity<T> get(String methodUrl, Class<T> type) {
        MultiValueMap<String, String> headers = getHeaders();

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        RestTemplate restTemplate = new RestTemplate();

        // like that http://localhost:8081/api/v1/smart/1/gas
        String url = "%s%s".formatted(baseHost(), methodUrl);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                type
        );
    }
}
