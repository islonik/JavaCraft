package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserHistory;
import my.javacraft.elastic.service.UserHistoryService;
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
public class UserClickResourceTest {

    @Mock
    UserHistoryService userHistoryService;

    @Test
    public void testCapture() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(userHistoryService);

        UpdateResponse updateResponse = Mockito.mock(UpdateResponse.class);
        when(userHistoryService.capture(any())).thenReturn(updateResponse);

        UserClick userClick = new UserClick();
        userClick.setDocumentId("did-1");
        userClick.setSearchType("Obligor");
        userClick.setSearchPattern("1111");

        ResponseEntity<UpdateResponse> response = hitCountResource.capture(userClick);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testGetHitCount() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(userHistoryService);

        GetResponse<UserHistory> getResponse = Mockito.mock(GetResponse.class);
        when(userHistoryService.getUserHistory(anyString())).thenReturn(getResponse);

        ResponseEntity<GetResponse<UserHistory>> response = hitCountResource
                .getHitCount("documentId");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testGetSearchHistory() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(userHistoryService);

        List<UserHistory> historyList = new ArrayList<>();
        when(userHistoryService.searchHistoryByUserId(anyString())).thenReturn(historyList);

        ResponseEntity<List<UserHistory>> response = hitCountResource
                .getSearchHistory("nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteIndex() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(userHistoryService);

        DeleteIndexResponse deleteIndexResponse = Mockito.mock(DeleteIndexResponse.class);
        when(userHistoryService.deleteIndex(anyString())).thenReturn(deleteIndexResponse);

        ResponseEntity<DeleteIndexResponse> response = hitCountResource
                .deleteIndex("nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteHitCountDocument() throws IOException {
        HitCountResource hitCountResource = new HitCountResource(userHistoryService);

        DeleteResponse deleteResponse = Mockito.mock(DeleteResponse.class);
        when(userHistoryService.deleteDocument(anyString(), anyString())).thenReturn(deleteResponse);

        ResponseEntity<DeleteResponse> response = hitCountResource
                .deleteHitCountDocument("hit_count", "nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

}
