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

    public ResponseEntity<Metric> put(String methodUrl, Metric metric) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");

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
}