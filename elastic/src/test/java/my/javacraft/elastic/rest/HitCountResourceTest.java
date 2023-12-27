package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import my.javacraft.elastic.model.HitCount;
import my.javacraft.elastic.service.HitCountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
public class HitCountResourceTest {

    @Mock
    HitCountService hitCountService;

    @Test
    public void testCapture() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(hitCountService);

        UpdateResponse updateResponse = Mockito.mock(UpdateResponse.class);
        when(hitCountService.capture(any())).thenReturn(updateResponse);

        HitCount hitCount = new HitCount();
        hitCount.setDocumentId("did-1");
        hitCount.setSearchType("Obligor");
        hitCount.setSearchPattern("1111");

        ResponseEntity<UpdateResponse> response = hitCountResource.capture(hitCount);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testGetHitCount() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(hitCountService);

        GetResponse<Map> getResponse = Mockito.mock(GetResponse.class);
        when(hitCountService.getHitCount(anyString())).thenReturn(getResponse);

        ResponseEntity<GetResponse<Map>> response = hitCountResource
                .getHitCount("documentId");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testGetSearchHistory() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(hitCountService);

        List<Map> historyList = new ArrayList<>();
        when(hitCountService.searchHistoryByUserId(anyString())).thenReturn(historyList);

        ResponseEntity<List<Map>> response = hitCountResource
                .getSearchHistory("nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteIndex() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(hitCountService);

        DeleteIndexResponse deleteIndexResponse = Mockito.mock(DeleteIndexResponse.class);
        when(hitCountService.deleteIndex(anyString())).thenReturn(deleteIndexResponse);

        ResponseEntity<DeleteIndexResponse> response = hitCountResource
                .deleteIndex("nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteHitCountDocument() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(hitCountService);

        DeleteResponse deleteResponse = Mockito.mock(DeleteResponse.class);
        when(hitCountService.deleteDocument(anyString(), anyString())).thenReturn(deleteResponse);

        ResponseEntity<DeleteResponse> response = hitCountResource
                .deleteHitCountDocument("hit_count", "nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

}
