package my.javacraft.elastic.cucumber.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.cucumber.config.CucumberSpringConfiguration;
import my.javacraft.elastic.model.Client;
import my.javacraft.elastic.model.SeekRequest;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class SearchDefinition {

    @Value("${server.port}")
    int port;

    @When("wildcard search for {string} in {string}")
    public void testWildcard(String pattern, String type, DataTable dataTable) throws IOException, InterruptedException {
        CucumberSpringConfiguration.waitAsElasticSearchIsEventuallyConsistentDB();

        String jsonBody = jsonBody(pattern, type);

        log.info("created json:\n" + jsonBody);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<LinkedHashMap<String, Object>>> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/services/search/wildcard".formatted(port),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        compareHttpResponseToDataTable(httpResponse, dataTable);
    }

    @When("fuzzy search for {string} in {string}")
    public void testFuzzy(String pattern, String type, DataTable dataTable) throws IOException, InterruptedException {
        CucumberSpringConfiguration.waitAsElasticSearchIsEventuallyConsistentDB();

        String jsonBody = jsonBody(pattern, type);

        log.info("created json:\n" + jsonBody);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<LinkedHashMap<String, Object>>> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/services/search/fuzzy".formatted(port),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        compareHttpResponseToDataTable(httpResponse, dataTable);
    }

    @When("span search for {string} in {string}")
    public void testSpan(String pattern, String type, DataTable dataTable) throws IOException, InterruptedException {
        CucumberSpringConfiguration.waitAsElasticSearchIsEventuallyConsistentDB();

        String jsonBody = jsonBody(pattern, type);

        log.info("created json:\n" + jsonBody);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<LinkedHashMap<String, Object>>> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/services/search/span".formatted(port),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        compareHttpResponseToDataTable(httpResponse, dataTable);
    }

    @When("search for {string} in {string}")
    public void testSearch(String pattern, String type, DataTable dataTable) throws IOException, InterruptedException {
        CucumberSpringConfiguration.waitAsElasticSearchIsEventuallyConsistentDB();

        String jsonBody = jsonBody(pattern, type);

        log.info("created json:\n" + jsonBody);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<LinkedHashMap<String, Object>>> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/services/search".formatted(port),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        compareHttpResponseToDataTable(httpResponse, dataTable);
    }

    private void compareHttpResponseToDataTable(
            ResponseEntity<List<LinkedHashMap<String, Object>>> httpResponse, DataTable dataTable) {
        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
        Assertions.assertEquals(dataTable.cells().size(), httpResponse.getBody().size());

        for (int i = 0; i < dataTable.cells().size(); i++) {
            LinkedHashMap<String, Object> map = httpResponse.getBody().get(i);
            List<String> row = dataTable.cells().get(i);

            for (String cell : row) {
                if (cell.contains("=")) {
                    String[] cellValues = cell.split("=");
                    String key = cellValues[0].trim();
                    String value = cellValues[1].trim();

                    Assertions.assertEquals(value, map.get(key).toString());
                }
            }
        }
    }

    private String jsonBody(String pattern, String type) throws JsonProcessingException {
        SeekRequest seekRequest = new SeekRequest();
        seekRequest.setType(type);
        seekRequest.setPattern(pattern);
        seekRequest.setClient(Client.WEB.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(seekRequest);
    }


}
