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
import java.util.Map;
import my.javacraft.elastic.model.HitCount;
import my.javacraft.elastic.model.HitCountTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
public class HitCountServiceTest {

    @Mock
    ElasticsearchClient esClient;

    @Test
    public void testGetCompositeId() {
        HitCount hitCount = new HitCount();
        hitCount.setUserId("nl8888");
        hitCount.setDocumentId("did-1");
        hitCount.setSearchType("Beneficial Owner");
        hitCount.setSearchPattern("-_+= 6789");

        HitCountService hitCountService = new HitCountService(esClient);

        Assertions.assertEquals("efbe53de-3ecb-306f-871f-70b6b4506080", hitCountService.getCompositeId(hitCount));
    }

    @Test
    public void testCapture() throws IOException {
        UpdateResponse updateResponse = Mockito.mock(UpdateResponse.class);
        Mockito.when(esClient.update(Mockito.any(UpdateRequest.class), Mockito.any())).thenReturn(updateResponse);
        HitCount hitCount = HitCountTest.createHitCount();

        HitCountService hitCountService = new HitCountService(esClient);
        Assertions.assertNotNull(hitCountService.capture(hitCount));
    }

    @Test
    public void testSearchHistoryByUserId() throws IOException {
        HitCountService hitCountService = new HitCountService(esClient);

        HitCount hitCount = HitCountTest.createHitCount();
        Map<String, Object> map = hitCountService.createInitialValues(hitCount);

        Hit<Map> hitMap = new Hit.Builder<Map>()
                .index(HitCountService.HIT_COUNT)
                .id(hitCountService.getCompositeId(hitCount))
                .source(map)
                .build();

        List<Hit<Map>> hitList = new ArrayList<>();
        hitList.add(hitMap);

        HitsMetadata<Map> hitsMetadata = Mockito.mock(HitsMetadata.class);
        Mockito.when(hitsMetadata.hits()).thenReturn(hitList);

        SearchResponse<Map> searchResponse = Mockito.mock(SearchResponse.class);
        Mockito.when(searchResponse.hits()).thenReturn(hitsMetadata);

        Mockito.when(esClient.search(Mockito.any(SearchRequest.class), Mockito.eq(Map.class))).thenReturn(searchResponse);

        List<Map> result = hitCountService.searchHistoryByUserId("nl8888");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        Map resultHit = result.get(0);
        Assertions.assertEquals(1L, resultHit.get("count"));
        Assertions.assertEquals("nl8888", resultHit.get("userId"));
        Assertions.assertEquals("did-1", resultHit.get("documentId"));
        Assertions.assertEquals("Beneficial Owner", resultHit.get("searchType"));
        Assertions.assertEquals("-+= 6789", resultHit.get("searchPattern"));
        Assertions.assertEquals("9e12450d-1824-3292-a556-b5c41de79803", resultHit.get("id"));
    }
}
