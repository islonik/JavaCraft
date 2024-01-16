package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.json.JsonData;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserHistory;
import org.springframework.stereotype.Service;

/**
 * Index 'hit_count' should be created with the 'updated' field set up as a 'date' format. See README.md.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Service
@RequiredArgsConstructor
public class UserHistoryService {

    static final String USER_HISTORY = "user_history";

    private final ElasticsearchClient esClient;

    public UpdateResponse capture(UserClick userClick) throws IOException {
        // default scripting language in Elasticsearch is 'painless'
        // It supports java8 syntax, but doesn't support the current datetime.
        String datetime = getCurrentDate();
        InlineScript inlineScript = new InlineScript.Builder()
                .source("""
                                ctx._source.count++;
                                ctx._source.updated=params['updated'];
                        """)
                .params("updated", JsonData.of(datetime))
                .build();
        Script script = new Script.Builder()
                .inline(inlineScript)
                .build();

        UserHistory userHistory = new UserHistory(datetime, userClick);
        UpdateRequest updateRequest = new UpdateRequest.Builder<>()
                .index(USER_HISTORY)
                .id(userHistory.getElasticId(userClick))
                .upsert(userHistory)
                .script(script)
                .build();
        return esClient.update(updateRequest, UserHistory.class);
    }

    public GetResponse<UserHistory> getUserHistory(String documentId) throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index(USER_HISTORY)
                .id(documentId)
                .build();

        return esClient.get(getRequest, UserHistory.class);
    }

    public List<UserHistory> searchHistoryByUserId(String userId) throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(USER_HISTORY)
                // search by userId
                .query(q -> q.term(t -> t
                        .field("userClick.userId")
                        .value(v -> v.stringValue(userId))
                ))
                .size(10) // limit result to 10 values
                // the result values with the highest count are going to be displayed
                .sort(so -> so.field(
                                FieldSort.of(f -> f
                                        .field("count")
                                        .order(SortOrder.Desc)
                                )
                        )
                ).build();

        return esClient.search(searchRequest, UserHistory.class)
                .hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(Hit::source)
                .toList();
    }

    public DeleteIndexResponse deleteIndex(String index) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest.Builder()
                .index(index)
                .build();
        return esClient.indices().delete(request);
    }

    public DeleteResponse deleteDocument(String index, String documentId) throws IOException {
        DeleteRequest request = new DeleteRequest.Builder()
                .index(index)
                .id(documentId)
                .build();
        return esClient.delete(request);
    }

    // returns: 2024-01-08T18:16:41.530571300Z
    private String getCurrentDate() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }
}
