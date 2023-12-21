package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.InlineScript;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import my.javacraft.elastic.model.HitCount;
import org.springframework.stereotype.Service;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
@RequiredArgsConstructor
public class HitCountService {

    private final ElasticsearchClient esClient;

    public UpdateResponse capture(HitCount hitCount) throws IOException {
        InlineScript inlineScript = new InlineScript.Builder()
                .source("ctx._source.count++")
                .build();
        Script script = new Script.Builder()
                .inline(inlineScript)
                .build();

        Map<String, Object> upsertJson = new HashMap<>();
        upsertJson.put("count", 1); // initial value; It won't be overridden
        upsertJson.put("userId", hitCount.getUserId());
        upsertJson.put("searchType", hitCount.getSearchType());
        upsertJson.put("searchPattern", hitCount.getSearchPattern());

        UpdateRequest updateRequest = new UpdateRequest.Builder<>()
                .index("hit_count")
                .id(hitCount.getDocumentId())
                .upsert(upsertJson)
                .script(script)
                .build();
        return esClient.update(updateRequest, Map.class);
    }

    public GetResponse<Map> getHitCount(String documentId) throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index("hit_count")
                .id(documentId)
                .build();

        return esClient.get(getRequest, Map.class);
    }

    public List<Map<String, String>> searchHistoryByUserId(String userId) throws IOException {
        SearchResponse<Map> search = esClient.search(s -> s
                        .index("hit_count")
                        .query(q -> q.term(t -> t
                                .field("userId")
                                .value(v -> v.stringValue(userId))
                        )),
                Map.class
        );

        List<Map<String, String>> mapList = new ArrayList<>();
        for (Hit<Map> hit: search.hits().hits()) {
            mapList.add(hit.source());
        }
        return mapList;
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
