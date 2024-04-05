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
    public void ingestMoviesJson(String pathToFile, Integer expectedItems, String index) throws IOException {
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

    @Test
    public void exportImdbTop250Movies() throws IOException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br, zstd");
        headers.set(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> httpResponse = restTemplate.exchange(
                "https://www.imdb.com/list/ls068082370/export?ref_=ttls_otexp",
                HttpMethod.GET,
                entity,
                String.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
        try (PrintWriter out = new PrintWriter("movies.csv")) {
            out.write(httpResponse.getBody());
        }
    }

    @Test
    public void transformCsvIntoJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        List<String> csvLines = Files.readAllLines(new File("movies.csv").toPath());

        List<Movie> movieList = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(false)
                .build();
        for (int i = 1; i < csvLines.size(); i++) {
            String movieLine = csvLines.get(i);

            String[] columns = parser.parseLine(movieLine);

            // Position,Const,Created,Modified,Description,Title,URL,Title Type,IMDb Rating,Runtime (mins),Year,Genres,Num Votes,Release Date,Directors
            var position = columns[0]; // ranking
            var imdbId = columns[1];
            //var created = columns[2];
            //var modified = columns[3];
            var desc = columns[4]; // name
            var title = columns[5]; // name
            //var url = columns[6];
            //var imdbType = columns[7];
            //var imdbRating = columns[8];
            //var runtime = columns[9];
            var releaseYear = columns[10]; // release year
            var genres = columns[11]; // genres
            //var votes = columns[12];
            //var releaseDate = columns[13];
            var director = columns[14]; // director

            log.info("Processing {}...", title);

            try {
                Thread.sleep(500); // try not to spam the WEB-service

                MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                HttpEntity<String> entity = new HttpEntity<>(null, headers);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> htmlResponse = restTemplate.exchange(
                        "http://www.omdbapi.com/?i=%s&apikey=1e60fc51".formatted(imdbId),
                        HttpMethod.GET,
                        entity,
                        String.class
                );
                Assertions.assertNotNull(htmlResponse);
                Assertions.assertNotNull(htmlResponse.getBody());
                TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
                Map<String, Object> movieMap = objectMapper.readValue(htmlResponse.getBody(), typeRef);
                desc = (String)movieMap.get("Plot");
            } catch (Exception e) {
                log.debug(e.getMessage());
            }

            Movie movie = new Movie();
            movie.setName(title);
            movie.setDirector(director);
            movie.setRanking(Integer.parseInt(position));
            movie.setReleaseYear(Integer.parseInt(releaseYear));
            movie.setGenres(Arrays.stream(genres.replaceAll(" ", "").split(",")).toList());
            movie.setSynopsis(desc);

            movieList.add(movie);
        }

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(movieList);

        try (PrintWriter out = new PrintWriter("movies.json")) {
            out.write(json);
        }

    }

    @Data
    public static class Movie {
        String name;
        String director;
        Integer ranking;
        Integer releaseYear;
        List<String> genres;
        String synopsis;

        public Movie() {}
    }

}
