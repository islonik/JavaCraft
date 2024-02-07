package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickTest;
import my.javacraft.elastic.model.UserHistory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
public class UserClickServiceTest {

    @Mock
    ElasticsearchClient esClient;

    @Test
    public void testCapture() throws IOException {
        UpdateResponse updateResponse = Mockito.mock(UpdateResponse.class);
        Mockito.when(esClient.update(Mockito.any(UpdateRequest.class), Mockito.any())).thenReturn(updateResponse);
        UserClick userClick = UserClickTest.createHitCount();

        UserHistoryService userHistoryService = new UserHistoryService(esClient);
        Assertions.assertNotNull(userHistoryService.capture(userClick));
    }

    @Test
    public void testSearchHistoryByUserId() throws IOException {
        UserHistoryService userHistoryService = new UserHistoryService(esClient);

        UserClick userClick = UserClickTest.createHitCount();
        UserHistory userHistory = new UserHistory("2024-01-08T18:16:41.530571300Z", userClick);

        Hit<UserHistory> hitMap = new Hit.Builder<UserHistory>()
                .index(UserHistoryService.USER_HISTORY)
                .id(userHistory.getElasticId(userClick))
                .source(userHistory)
                .build();

        List<Hit<UserHistory>> hitList = new ArrayList<>();
        hitList.add(hitMap);

        HitsMetadata<UserHistory> hitsMetadata = Mockito.mock(HitsMetadata.class);
        Mockito.when(hitsMetadata.hits()).thenReturn(hitList);

        SearchResponse<UserHistory> searchResponse = Mockito.mock(SearchResponse.class);
        Mockito.when(searchResponse.hits()).thenReturn(hitsMetadata);

        Mockito.when(esClient.search(Mockito.any(SearchRequest.class), Mockito.eq(UserHistory.class))).thenReturn(searchResponse);

        List<UserHistory> result = userHistoryService.searchHistoryByUserId("nl8888", 10);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        UserHistory resultHit = result.getFirst();
        Assertions.assertEquals(1L, resultHit.getCount());
        Assertions.assertEquals("nl8888", resultHit.getUserClick().getUserId());
        Assertions.assertEquals("did-1", resultHit.getUserClick().getDocumentId());
        Assertions.assertEquals("Beneficial Owner", resultHit.getUserClick().getSearchType());
        Assertions.assertEquals("-+= 6789", resultHit.getUserClick().getSearchPattern());
        Assertions.assertEquals("9e12450d-1824-3292-a556-b5c41de79803", resultHit.getElasticId());
    }
}
