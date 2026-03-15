package my.javacraft.elastic.service.activity;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonpUtils;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.UserActivity;
import org.springframework.stereotype.Service;

/*
 * Popular means something has high overall engagement or usage over a longer period.
 *
 * Typical signals used:
 *
 * 1) total views
 * 2) total likes
 * 3) total downloads
 * 4) total purchases
 * 5) long-term user activity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityPopularService {

    private final ElasticsearchClient esClient;

    public List<UserActivity> retrievePopularUserSearches(String userId, int searchLimitSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(UserActivityService.INDEX_USER_HISTORY)
                // search by userId
                .query(q -> q.term(t -> t
                        .field(UserActivityService.USER_ID)
                        .value(v -> v.stringValue(userId))
                ))
                .size(searchLimitSize) // limit result to N values
                // the result values with the highest count are going to be displayed
                .sort(so -> so.field(
                                FieldSort.of(f -> f
                                        .field(UserActivityService.COUNT)
                                        .order(SortOrder.Desc)
                                )
                        )
                ).build();

        // use -Dlogging.level.tracer=TRACE to print a full CURL statement or see
        log.debug("JSON representation of a query: {}", JsonpUtils.toJsonString(searchRequest, esClient._jsonpMapper()));
        List<UserActivity> userActivityList = esClient.search(searchRequest, UserActivity.class)
                .hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(Hit::source)
                .toList();
        log.trace(userActivityList.toString());
        return userActivityList;
    }
}
