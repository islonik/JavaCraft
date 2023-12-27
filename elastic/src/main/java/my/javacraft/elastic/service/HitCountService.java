package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import my.javacraft.elastic.model.HitCount;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
@RequiredArgsConstructor
public class HitCountService {

    private static final String HIT_COUNT = "hit_count";

    private final ElasticsearchClient esClient;

    public String getCompositeId(HitCount hitCount) {
        return UriUtils.encode("%s_%s_%s".formatted(
                hitCount.getDocumentId(),
                hitCount.getSearchType(),
                hitCount.getSearchPattern()
        ), StandardCharsets.UTF_8);
    }

    public UpdateResponse capture(HitCount hitCount) throws IOException {
        InlineScript inlineScript = new InlineScript.Builder()
                .source("ctx._source.count++")
                .build();
        Script script = new Script.Builder()
                .inline(inlineScript)
                .build();

        // initial values
        Map<String, Object> initialValues = new HashMap<>();
        initialValues.put("count", 1L);
        initialValues.put("userId", hitCount.getUserId());
        initialValues.put("searchType", hitCount.getSearchType());
        initialValues.put("searchPattern", hitCount.getSearchPattern());

        UpdateRequest updateRequest = new UpdateRequest.Builder<>()
                .index(HIT_COUNT)
                .id(getCompositeId(hitCount))
                .upsert(initialValues)
                .script(script)
                .build();
        return esClient.update(updateRequest, Map.class);
    }

    public GetResponse<Map> getHitCount(String documentId) throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index(HIT_COUNT)
                .id(documentId)
                .build();

        return esClient.get(getRequest, Map.class);
    }

    public List<Map> searchHistoryByUserId(String userId) throws IOException {
        SearchResponse<Map> search = esClient.search(s -> s
                        .index(HIT_COUNT)
                        // search by userId
                        .query(q -> q.term(t -> t
                                .field("userId")
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
                        ),
                Map.class
        );

        return search.hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> { // source doesn't contain document id
                    Map map = hit.source();
                    map.put("id", hit.id());
                    return map;
                })
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
}
