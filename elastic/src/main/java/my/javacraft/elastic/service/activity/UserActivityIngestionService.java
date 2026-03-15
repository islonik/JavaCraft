package my.javacraft.elastic.service.activity;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
import my.javacraft.elastic.model.UserActivity;
import org.springframework.stereotype.Service;

/**
 * Index 'user-activity' should be created with the 'updated' field set up as a 'date' format. See README.md.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityIngestionService {

    private static final int RETRY_ATTEMPTS = 3;

    private final ElasticsearchClient esClient;

    public UserClickResponse ingestUserClick(UserClick userClick, String datetime) throws IOException {
        // default scripting language in Elasticsearch is 'painless'
        // It supports java8 syntax, but doesn't support the current datetime.

        Script script = Script.of(s -> s
                .source(createScriptOrigin())
                .params(UserActivityService.UPDATED, JsonData.of(datetime))
        );

        UserActivity userActivity = new UserActivity(datetime, userClick);
        String documentId = userActivity.getElasticId(userClick);
        UpdateRequest<UserActivity, Object> updateRequest = new UpdateRequest.Builder<UserActivity, Object>()
                .index(UserActivityService.INDEX_USER_HISTORY)
                .id(documentId)
                .upsert(userActivity)
                .script(script)
                .retryOnConflict(RETRY_ATTEMPTS)
                .build();

        // use -Dlogging.level.tracer=TRACE to print a full curl statement
        log.debug("JSON representation of a query: {}", JsonpUtils.toJsonString(updateRequest, esClient._jsonpMapper()));
        // execute request to ES cluster
        UpdateResponse<UserActivity> updateResponse = esClient.update(updateRequest, UserActivity.class);

        // prepare response
        UserClickResponse userClickResponse = new UserClickResponse();
        userClickResponse.setDocumentId(documentId);
        userClickResponse.setResult(updateResponse.result());
        return userClickResponse;
    }

    String createScriptOrigin() {
        return """
                ctx._source.%s++;
                ctx._source.%s=params['%s'];
                """.formatted(UserActivityService.COUNT, UserActivityService.UPDATED, UserActivityService.UPDATED);
    }
}
