package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
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

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class UserClickResourceTest {

    @Mock
    UserHistoryService userHistoryService;

    @Test
    public void testCapture() throws IOException {
        UserHistoryResource userHistoryResource = new UserHistoryResource(userHistoryService);

        UserClickResponse userClickResponse = Mockito.mock(UserClickResponse.class);
        when(userHistoryService.capture(any())).thenReturn(userClickResponse);

        UserClick userClick = new UserClick();
        userClick.setDocumentId("did-1");
        userClick.setSearchType("Obligor");
        userClick.setSearchPattern("1111");

        ResponseEntity<UserClickResponse> response = userHistoryResource.capture(userClick);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testGetHitCount() throws IOException {
        UserHistoryResource userHistoryResource = new UserHistoryResource(userHistoryService);

        UserHistory userHistory = Mockito.mock(UserHistory.class);
        GetResponse<UserHistory> getResponse = new GetResponse.Builder<UserHistory>()
                .index(UserHistoryService.USER_HISTORY)
                .found(true)
                .id("part-of-mock-so-any-id")
                .source(userHistory)
                .build();
        when(userHistoryService.getUserHistory(anyString())).thenReturn(getResponse);

        ResponseEntity<GetResponse<UserHistory>> response = userHistoryResource
                .getHitCount("documentId");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testGetSearchHistory() throws IOException {
        UserHistoryResource userHistoryResource = new UserHistoryResource(userHistoryService);

        List<UserHistory> historyList = new ArrayList<>();
        when(userHistoryService.searchHistoryByUserId(anyString(), anyInt())).thenReturn(historyList);

        ResponseEntity<List<UserHistory>> response = userHistoryResource
                .getSearchHistory("nl88888", "10");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteIndex() throws IOException {
        UserHistoryResource userHistoryResource = new UserHistoryResource(userHistoryService);

        DeleteIndexResponse deleteIndexResponse = Mockito.mock(DeleteIndexResponse.class);
        when(userHistoryService.deleteIndex(anyString())).thenReturn(deleteIndexResponse);

        ResponseEntity<DeleteIndexResponse> response = userHistoryResource
                .deleteIndex("nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteHitCountDocument() throws IOException {
        UserHistoryResource userHistoryResource = new UserHistoryResource(userHistoryService);

        DeleteResponse deleteResponse = Mockito.mock(DeleteResponse.class);
        when(userHistoryService.deleteDocument(anyString(), anyString())).thenReturn(deleteResponse);

        ResponseEntity<DeleteResponse> response = userHistoryResource
                .deleteHitCountDocument("hit_count", "nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

}
