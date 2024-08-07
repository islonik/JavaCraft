package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.service.history.UserHistoryService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ElasticsearchClient esClient;
    private final DateService dateService;

    public Long removeOldHistoryRecords() {
        try {
            RangeQuery rangeQuery = new RangeQuery.Builder()
                    .field(UserHistoryService.UPDATED)
                    .lte(JsonData.of(dateService.getNDaysBeforeDate(UserHistoryService.SIX_MONTHS)))
                    .build();
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder()
                    .index(UserHistoryService.INDEX_USER_HISTORY)
                    .query(rangeQuery._toQuery())
                    .build();

            // use -Dlogging.level.tracer=TRACE to print a full curl statement
            DeleteByQueryResponse deleteByQueryResponse = esClient.deleteByQuery(deleteByQueryRequest);
            return deleteByQueryResponse.deleted();
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0L;
        }
    }
}
