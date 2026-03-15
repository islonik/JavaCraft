package my.javacraft.elastic.service.activity;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.FieldCollapse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.NamedValue;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.UserActivity;
import my.javacraft.elastic.service.DateService;
import org.springframework.stereotype.Service;

/*
 * Trending means something is growing rapidly right now.
 *
 * Platforms detect sudden spikes in activity.
 *
 * Typical signals:
 *
 * 1) rapid increase in views
 * 2) sudden rise in searches
 * 3) engagement growth rate
 * 4) activity in the last minutes/hours
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityTrendingService {

    private final ElasticsearchClient esClient;
    private final DateService dateService;

    public List<UserActivity> retrieveTrendingUserSearches(int size) throws IOException {
        SearchRequest searchRequest = prepareTrendingSearchRequest(size);

        SearchResponse<UserActivity> searchResponse = esClient.search(searchRequest, UserActivity.class);

        return prepareTrendingActivityResult(searchResponse);
    }

    private SearchRequest prepareTrendingSearchRequest(int size) {
        final FieldSort fieldSort = FieldSort.of(f -> f
                .field(UserActivityService.UPDATED)
                .order(SortOrder.Desc)
        );

        List<Query> mustQueryList = new ArrayList<>();
        RangeQuery rangeQuery = RangeQuery.of(r -> r
                .date(d -> d
                        .field(UserActivityService.UPDATED)
                        .lte(dateService.getCurrentDate())
                        .gte(dateService.getNDaysBeforeDate(UserActivityService.SEVEN_DAYS))
                )
        );
        mustQueryList.add(rangeQuery._toQuery());

        List<NamedValue<SortOrder>> namedValueList = new ArrayList<>();
        namedValueList.add(new NamedValue<>("_count", SortOrder.Desc));
        namedValueList.add(new NamedValue<>(UserActivityService.COUNT, SortOrder.Desc));

        // provided for aggregation
        // query size should be more
        int querySize = Math.min(size * 10, UserActivityService.MAX_VALUES);

        // similar to DISTINCT in SQL
        FieldCollapse.Builder fieldCollapse = new FieldCollapse.Builder();
        fieldCollapse.field(UserActivityService.USER_ID);

        Query boolQuery = new BoolQuery.Builder()
                .must(mustQueryList)
                .build()
                ._toQuery();

        return new SearchRequest.Builder()
                .index(UserActivityService.INDEX_USER_ACTIVITY)
                .query(boolQuery)
                .aggregations(UserActivityService.RECORD_ID, a1 -> a1
                        .terms(t -> t
                                .field(UserActivityService.RECORD_ID)
                                .size(size)
                                .order(namedValueList)
                        )
                        .aggregations(UserActivityService.COUNT, a2 -> a2
                                .sum(s -> s.field(UserActivityService.COUNT))
                        )
                )
                .size(querySize)
                .sort(so -> so.field(fieldSort))
                .collapse(fieldCollapse.build())
                .build();
    }

    private List<UserActivity> prepareTrendingActivityResult(SearchResponse<UserActivity> searchResponse) {
        List<StringTermsBucket> buckets = searchResponse
                .aggregations()
                .get(UserActivityService.RECORD_ID)
                .sterms()
                .buckets()
                .array();

        // get recordIds in correct order
        List<String> recordIds = new ArrayList<>();
        for (StringTermsBucket bucket : buckets) {
            recordIds.add(bucket.key().stringValue());
        }

        // define order for values
        Map<String, UserActivity> resultMap = new LinkedHashMap<>();
        for (String recordId : recordIds) {
            resultMap.put(recordId, null);
        }

        // get ALL values which were used in aggregation
        List<UserActivity> allRecords = searchResponse
                .hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(Hit::source)
                .toList();

        // populate values if key exist, but value is null
        for (UserActivity curr : allRecords) {
            String key = curr.getRecordId();
            if (resultMap.containsKey(key) && resultMap.get(key) == null) {
                resultMap.putIfAbsent(key, curr);
            }
        }
        // remove not populated values
        resultMap.values().removeAll(Collections.singleton(null));
        // return final result
        return new ArrayList<>(resultMap.values());
    }
}
