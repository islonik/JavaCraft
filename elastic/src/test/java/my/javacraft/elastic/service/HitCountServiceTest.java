package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import my.javacraft.elastic.model.HitCount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        Assertions.assertEquals("ZGlkLTFfQmVuZWZpY2lhbCBPd25lcl8tXys9IDY3ODk=", hitCountService.getCompositeId(hitCount));
    }
}
