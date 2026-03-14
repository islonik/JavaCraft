package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.service.history.UserHistoryService;
import org.springframework.stereotype.Service;

/**
 * Handles creation of Elasticsearch indexes with their field mappings.
 * <p>
 * Two groups of indexes are managed:
 * <ul>
 *   <li><b>user-history</b> – used by UserHistoryController for ingestion and retrieval</li>
 *   <li><b>books / movies / music</b> – used by SearchController for full-text search</li>
 * </ul>
 * Index names for the search group match the lowercased {@code SeekType} enum values
 * that are listed in {@code metadata.json}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    static final String INDEX_BOOKS = "books";
    static final String INDEX_MOVIES = "movies";
    static final String INDEX_MUSIC = "music";

    private final ElasticsearchClient esClient;

    /**
     * Creates the {@code user-history} index with typed field mappings.
     * <p>
     * Field types are chosen to match the queries used in
     * {@link UserHistoryService}, {@code UserHistoryPopularService}, and
     * {@code UserHistoryTrendingService}:
     * <ul>
     *   <li>{@code count} – {@code long} (incremented on upsert)</li>
     *   <li>{@code updated} – {@code date} with ISO-8601 format (used in range queries)</li>
     *   <li>{@code userId}, {@code recordId}, {@code searchType}, {@code elasticId}
     *       – {@code keyword} (exact-match term queries and aggregations)</li>
     *   <li>{@code searchValue} – {@code text} (full-text field, not queried directly)</li>
     * </ul>
     */
    public CreateIndexResponse createUserHistoryIndex() throws IOException {
        log.info("creating index '{}'...", UserHistoryService.INDEX_USER_HISTORY);

        Map<String, Property> properties = new LinkedHashMap<>();
        properties.put("count", Property.of(p -> p.long_(l -> l)));
        properties.put("updated", Property.of(p -> p.date(d -> d.format("strict_date_optional_time"))));
        properties.put("elasticId", Property.of(p -> p.keyword(k -> k)));
        properties.put("userId", Property.of(p -> p.keyword(k -> k)));
        properties.put("recordId", Property.of(p -> p.keyword(k -> k)));
        properties.put("searchType", Property.of(p -> p.keyword(k -> k)));
        properties.put("searchValue", Property.of(p -> p.text(t -> t)));

        CreateIndexResponse response = createIndex(UserHistoryService.INDEX_USER_HISTORY, properties);
        log.info(
                "index '{}' created (acknowledged={})",
                UserHistoryService.INDEX_USER_HISTORY,
                response.acknowledged()
        );
        return response;
    }

    /**
     * Creates the {@code books} index.
     * Fields match the search fields configured in {@code metadata.json}:
     * {@code name}, {@code author}, {@code synopsis}.
     */
    public CreateIndexResponse createBooksIndex() throws IOException {
        return createSearchIndex(INDEX_BOOKS, "name", "author", "synopsis");
    }

    /**
     * Creates the {@code movies} index.
     * Fields match the search fields configured in {@code metadata.json}:
     * {@code name}, {@code director}, {@code synopsis}.
     */
    public CreateIndexResponse createMoviesIndex() throws IOException {
        return createSearchIndex(INDEX_MOVIES, "name", "director", "synopsis");
    }

    /**
     * Creates the {@code music} index.
     * Fields match the search fields configured in {@code metadata.json}:
     * {@code band}, {@code album}, {@code name}, {@code lyrics}.
     */
    public CreateIndexResponse createMusicIndex() throws IOException {
        return createSearchIndex(INDEX_MUSIC, "band", "album", "name", "lyrics");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a search index where every field is mapped as {@code text}
     * to support full-text queries (wildcard, fuzzy, interval, span).
     */
    private CreateIndexResponse createSearchIndex(String indexName, String... textFields) throws IOException {
        log.info("creating search index '{}'...", indexName);

        Map<String, Property> properties = new LinkedHashMap<>();
        for (String field : textFields) {
            properties.put(field, Property.of(p -> p.text(t -> t)));
        }

        CreateIndexResponse response = createIndex(indexName, properties);
        log.info("search index '{}' created (acknowledged={})", indexName, response.acknowledged());
        return response;
    }

    private CreateIndexResponse createIndex(String indexName, Map<String, Property> properties) throws IOException {
        ElasticsearchIndicesClient indicesClient = esClient.indices();
        CreateIndexRequest request = CreateIndexRequest.of(b -> b
                .index(indexName)
                .mappings(m -> m.properties(properties))
        );
        return indicesClient.create(request);
    }
}
