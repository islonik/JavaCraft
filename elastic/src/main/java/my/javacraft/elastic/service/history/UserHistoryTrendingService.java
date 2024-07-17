package my.javacraft.elastic.service.history;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery.Builder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.UserHistory;
import my.javacraft.elastic.service.DateService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserHistoryTrendingService {

    private final ElasticsearchClient esClient;
    private final DateService dateService;

    public List<UserHistory> retrieveTrendingUserSearches(int size) throws IOException {
        SearchRequest searchRequest = prepareTrendingSearchRequest(size);

        SearchResponse<UserHistory> searchResponse = esClient.search(searchRequest, UserHistory.class);

        return prepareTrendingHistoryResult(searchResponse);
    }

    private SearchRequest prepareTrendingSearchRequest(int size) {
        final FieldSort fieldSort = FieldSort.of(f -> f
                .field(UserHistoryService.UPDATED)
                .order(SortOrder.Desc)
        );

        List<Query> mustQueryList = new ArrayList<>();
        RangeQuery.Builder rangeQueryBuilder = new Builder();
        rangeQueryBuilder.field(UserHistoryService.UPDATED);
        rangeQueryBuilder.lte(JsonData.of(dateService.getCurrentDate()));
        rangeQueryBuilder.gte(JsonData.of(dateService.getNDaysBeforeDate(UserHistoryService.SEVEN_DAYS)));
        mustQueryList.add(rangeQueryBuilder.build()._toQuery());

        List<NamedValue<SortOrder>> namedValueList = new ArrayList<>();
        namedValueList.add(new NamedValue<>("_count", SortOrder.Desc));
        namedValueList.add(new NamedValue<>(UserHistoryService.COUNT, SortOrder.Desc));

        Query boolQuery = new BoolQuery.Builder()
                .must(mustQueryList)
                .build()
                ._toQuery();

        return new SearchRequest.Builder()
                .index(UserHistoryService.INDEX_USER_HISTORY)
                .query(boolQuery)
                .aggregations(UserHistoryService.RECORD_ID, a1 -> a1
                        .terms(t -> t
                                .field(UserHistoryService.RECORD_ID)
                                .size(size)
                                .order(namedValueList)
                        )
                        .aggregations(UserHistoryService.COUNT, a2 -> a2
                                .sum(s -> s.field(UserHistoryService.COUNT))
                        )
                )
                .size(size)
                .sort(so -> so.field(fieldSort))
                .build();
    }

    private List<UserHistory> prepareTrendingHistoryResult(SearchResponse<UserHistory> searchResponse) {
        List<StringTermsBucket> buckets = searchResponse
                .aggregations()
                .get(UserHistoryService.RECORD_ID)
                .sterms()
                .buckets()
                .array();

        // get recordIds in correct order
        List<String> recordIds = new ArrayList<>();
        for (StringTermsBucket bucket : buckets) {
            recordIds.add(bucket.key().stringValue());
        }

        // define order for values
        Map<String, UserHistory> resultMap = new LinkedHashMap<>();
        for (String recordId : recordIds) {
            resultMap.put(recordId, null);
        }

        // get ALL values which were used in aggregation
        List<UserHistory> allRecords = searchResponse
                .hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(Hit::source)
                .toList();

        // populate values
        for (UserHistory curr : allRecords) {
            String key = curr.getRecordId();
            resultMap.putIfAbsent(key, curr);
        }
        return new ArrayList<>(resultMap.values());
    }
}
