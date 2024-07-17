package my.javacraft.elastic.service.history;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.InlineScript;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpUtils;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
import my.javacraft.elastic.model.UserHistory;
import org.springframework.stereotype.Service;

/**
 * Index 'user-history' should be created with the 'updated' field set up as a 'date' format. See README.md.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserHistoryIngestionService {

    private static final int RETRY_ATTEMPTS = 3;

    private final ElasticsearchClient esClient;

    public UserClickResponse ingestUserClick(UserClick userClick, String datetime) throws IOException {
        // default scripting language in Elasticsearch is 'painless'
        // It supports java8 syntax, but doesn't support the current datetime.
        InlineScript inlineScript = new InlineScript.Builder()
                .source(createInlineScript())
                .params(UserHistoryService.UPDATED, JsonData.of(datetime))
                .build();
        Script script = new Script.Builder()
                .inline(inlineScript)
                .build();

        UserHistory userHistory = new UserHistory(datetime, userClick);
        String documentId = userHistory.getElasticId(userClick);
        UpdateRequest<UserHistory, Object> updateRequest = new UpdateRequest.Builder<UserHistory, Object>()
                .index(UserHistoryService.INDEX_USER_HISTORY)
                .id(documentId)
                .upsert(userHistory)
                .script(script)
                .retryOnConflict(RETRY_ATTEMPTS)
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
                """.formatted(UserHistoryService.COUNT, UserHistoryService.UPDATED, UserHistoryService.UPDATED);
    }
}
