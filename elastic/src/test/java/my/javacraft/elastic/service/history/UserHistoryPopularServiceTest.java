package my.javacraft.elastic.service.history;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
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
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked"})
@ExtendWith(MockitoExtension.class)
public class UserHistoryPopularServiceTest {

    @Mock
    ElasticsearchClient esClient;

    @Test
    public void testSearchHistoryByUserId() throws IOException {
        UserHistoryPopularService userHistoryPopularService = new UserHistoryPopularService(esClient);

        UserClick userClick = UserClickTest.createHitCount();
        UserHistory userHistory = new UserHistory("2024-01-08T18:16:41.530571300Z", userClick);

        Hit<UserHistory> hitMap = new Hit.Builder<UserHistory>()
                .index(UserHistoryService.INDEX_USER_HISTORY)
                .id(userHistory.getElasticId(userClick))
                .source(userHistory)
                .build();

        List<Hit<UserHistory>> hitList = new ArrayList<>();
        hitList.add(hitMap);

        HitsMetadata<UserHistory> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(hitList);

        SearchResponse<UserHistory> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(esClient._jsonpMapper()).thenReturn(new JacksonJsonpMapper());
        when(esClient.search(any(SearchRequest.class), eq(UserHistory.class))).thenReturn(searchResponse);

        List<UserHistory> result = userHistoryPopularService.retrievePopularUserSearches("nl8888", 10);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        UserHistory resultHit = result.getFirst();
        Assertions.assertEquals(1L, resultHit.getCount());
        Assertions.assertEquals("nl8888", resultHit.getUserClick().getUserId());
        Assertions.assertEquals("12345", resultHit.getUserClick().getRecordId());
        Assertions.assertEquals("People", resultHit.getUserClick().getSearchType());
        Assertions.assertEquals("Nikita", resultHit.getUserClick().getSearchPattern());
        Assertions.assertEquals("12345-People-nl8888", resultHit.getElasticId());
    }


}
