package my.javacraft.elastic.cucumber.step;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.cucumber.java.en.Given;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;
@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class IngestionDefinition {

    @Value("${spring.elastic.cluster.port}")
    int port;

    @Autowired
    ElasticsearchClient esClient;

    @Given("ingest {string} json file with {int} entities in {string} index")
    public void ingestJson(String pathToFile, Integer expectedItems, String index) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File resource = new ClassPathResource(pathToFile).getFile();
        TypeReference<List<LinkedHashMap<String, Object>>> typeRef = new TypeReference<>() {};
        List<LinkedHashMap<String, Object>> movies = objectMapper.readValue(resource, typeRef);

        Assertions.assertNotNull(movies);
        Assertions.assertEquals(expectedItems, movies.size());

        for (LinkedHashMap<String, Object> entity : movies) {
            String id = createId(entity);
            UpdateRequest<Object, Object> updateRequest = new UpdateRequest.Builder<>()
                    .index(index)
                    .id(id)
                    .doc(entity)
                    .upsert(entity)
                    .build();

            UpdateResponse<Object> updateResponse = esClient.update(updateRequest, Object.class);
            log.info("document with id = '{}' was ingested with the result '{}'",
                    id, updateResponse.result().toString()
            );
        }
    }

    private String createId(LinkedHashMap<String, Object> entity) {
        String id = "";
        if (entity.containsKey("name")) {
            id += ((String)entity.get("name")).toLowerCase().replaceAll(" ", "-");
        }
        if (entity.containsKey("release_year")) {
            if (!id.isEmpty()) {
                id += "-";
            }
            id += entity.get("release_year");
        }
        return id;
    }

}
