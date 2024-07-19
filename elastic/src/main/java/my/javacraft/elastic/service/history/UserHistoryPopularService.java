package my.javacraft.elastic.service.history;

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
import my.javacraft.elastic.model.UserHistory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHistoryPopularService {

    private final ElasticsearchClient esClient;

    public List<UserHistory> retrievePopularUserSearches(String userId, int searchLimitSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(UserHistoryService.INDEX_USER_HISTORY)
                // search by userId
                .query(q -> q.term(t -> t
                        .field(UserHistoryService.USER_ID)
                        .value(v -> v.stringValue(userId))
                ))
                .size(searchLimitSize) // limit result to N values
                // the result values with the highest count are going to be displayed
                .sort(so -> so.field(
                                FieldSort.of(f -> f
                                        .field(UserHistoryService.COUNT)
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
}
