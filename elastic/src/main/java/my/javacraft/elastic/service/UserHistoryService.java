package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpUtils;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
import my.javacraft.elastic.model.UserHistory;
import org.springframework.stereotype.Service;

/**
 * Index 'hit_count' should be created with the 'updated' field set up as a 'date' format. See README.md.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserHistoryService {

    public static final String USER_HISTORY = "user_history";
    public static final String COUNT = "count";
    public static final String UPDATED = "updated";

    private final ElasticsearchClient esClient;
    private final DateService dateService;

    public UserClickResponse capture(UserClick userClick, String datetime) throws IOException {
        // default scripting language in Elasticsearch is 'painless'
        // It supports java8 syntax, but doesn't support the current datetime.
        InlineScript inlineScript = new InlineScript.Builder()
                .source(createInlineScript())
                .params(UPDATED, JsonData.of(datetime))
                .build();
        Script script = new Script.Builder()
                .inline(inlineScript)
                .build();

        UserHistory userHistory = new UserHistory(datetime, userClick);
        String documentId = userHistory.getElasticId(userClick);
        UpdateRequest<UserHistory, Object> updateRequest = new UpdateRequest.Builder<UserHistory, Object>()
                .index(USER_HISTORY)
                .id(documentId)
                .upsert(userHistory)
                .script(script)
                .retryOnConflict(10) // retry 10 times to execute an update
                .build();

        // use -Dlogging.level.tracer=TRACE to print a full curl statement
        log.debug("JSON representation of a query: " + JsonpUtils.toJsonString(updateRequest, esClient._jsonpMapper()));
        // execute request to ES cluster
        UpdateResponse<UserHistory> updateResponse = esClient.update(updateRequest, UserHistory.class);

        // prepare response
        UserClickResponse userClickResponse = new UserClickResponse();
        userClickResponse.setDocumentId(documentId);
        userClickResponse.setResult(updateResponse.result());
        return userClickResponse;
    }

    String createInlineScript() {
        return """
                ctx._source.%s++;
                ctx._source.%s=params['%s'];
                """.formatted(COUNT, UPDATED, UPDATED);
    }

    public GetResponse<UserHistory> getUserHistoryByDocumentId(String documentId) throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index(USER_HISTORY)
                .id(documentId)
                .build();

        return esClient.get(getRequest, UserHistory.class);
    }

    public List<UserHistory> searchHistoryByUserId(String userId, int searchLimitSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(USER_HISTORY)
                // search by userId
                .query(q -> q.term(t -> t
                        .field("userClick.userId")
                        .value(v -> v.stringValue(userId))
                ))
                .size(searchLimitSize) // limit result to N values
                // the result values with the highest count are going to be displayed
                .sort(so -> so.field(
                                FieldSort.of(f -> f
                                        .field(COUNT)
                                        .order(SortOrder.Desc)
                                )
                        )
                ).build();

        // use -Dlogging.level.tracer=TRACE to print a full CURL statement or see
        log.debug("JSON representation of a query: " + JsonpUtils.toJsonString(searchRequest, esClient._jsonpMapper()));
        List<UserHistory> userHistoryList = esClient.search(searchRequest, UserHistory.class)
                .hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(Hit::source)
                .toList();
        log.trace(userHistoryList.toString());
        return userHistoryList;
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


}
