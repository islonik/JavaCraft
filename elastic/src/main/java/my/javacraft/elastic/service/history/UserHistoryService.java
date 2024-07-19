package my.javacraft.elastic.service.history;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.UserHistory;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserHistoryService {

    // we keep 180 days or close to 6 months of data in the index
    public static final int SIX_MONTHS = 180;
    public static final int SEVEN_DAYS = 7;
    public static final int MAX_VALUES = 10000; // Elasticsearch limit
    public static final String INDEX_USER_HISTORY = "user-history";
    public static final String COUNT = "count";
    public static final String UPDATED = "updated";
    public static final String RECORD_ID = "recordId";
    public static final String USER_ID = "userId";

    private final ElasticsearchClient esClient;

    public GetResponse<UserHistory> getUserHistoryByDocumentId(String documentId) throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index(INDEX_USER_HISTORY)
                .id(documentId)
                .build();

        return esClient.get(getRequest, UserHistory.class);
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
