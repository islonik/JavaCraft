package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
import my.javacraft.elastic.model.UserHistory;
import my.javacraft.elastic.service.DateService;
import my.javacraft.elastic.service.history.UserHistoryIngestionService;
import my.javacraft.elastic.service.history.UserHistoryPopularService;
import my.javacraft.elastic.service.history.UserHistoryService;
import my.javacraft.elastic.service.history.UserHistoryTrendingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserHistoryControllerTest {

    @Mock
    DateService dateService;
    @Mock
    UserHistoryService userHistoryService;
    @Mock
    UserHistoryPopularService userHistoryPopularService;
    @Mock
    UserHistoryTrendingService userHistoryTrendingService;
    @Mock
    UserHistoryIngestionService userHistoryIngestionService;

    @Test
    public void testCapture() throws IOException {
        UserHistoryController userHistoryController = new UserHistoryController(
                dateService,
                userHistoryService,
                userHistoryPopularService,
                userHistoryTrendingService,
                userHistoryIngestionService
        );

        when(dateService.getCurrentDate()).thenReturn("2024-01-15");

        UserClickResponse userClickResponse = Mockito.mock(UserClickResponse.class);
        when(userHistoryIngestionService.ingestUserClick(any(), anyString())).thenReturn(userClickResponse);

        UserClick userClick = new UserClick();
        userClick.setRecordId("did-1");
        userClick.setSearchType("Obligor");
        userClick.setSearchPattern("1111");

        ResponseEntity<UserClickResponse> response = userHistoryController.captureUserClick(userClick);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testGetHitCount() throws IOException {
        UserHistoryController userHistoryController = new UserHistoryController(
                dateService,
                userHistoryService,
                userHistoryPopularService,
                userHistoryTrendingService,
                userHistoryIngestionService
        );

        UserHistory userHistory = Mockito.mock(UserHistory.class);
        GetResponse<UserHistory> getResponse = new GetResponse.Builder<UserHistory>()
                .index(UserHistoryService.INDEX_USER_HISTORY)
                .found(true)
                .id("part-of-mock-so-any-id")
                .source(userHistory)
                .build();
        when(userHistoryService.getUserHistoryByDocumentId(anyString())).thenReturn(getResponse);

        ResponseEntity<GetResponse<UserHistory>> response = userHistoryController
                .getHitCount("documentId");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testPopularSearchHistory() throws IOException {
        UserHistoryController userHistoryController = new UserHistoryController(
                dateService,
                userHistoryService,
                userHistoryPopularService,
                userHistoryTrendingService,
                userHistoryIngestionService
        );

        List<UserHistory> historyList = new ArrayList<>();
        when(userHistoryPopularService.retrievePopularUserSearches(anyString(), anyInt())).thenReturn(historyList);

        ResponseEntity<List<UserHistory>> response = userHistoryController
                .retrievePopularUserSearches("nl88888", "10");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testTrendingSearchHistory() throws IOException {
        UserHistoryController userHistoryController = new UserHistoryController(
                dateService,
                userHistoryService,
                userHistoryPopularService,
                userHistoryTrendingService,
                userHistoryIngestionService
        );

        List<UserHistory> historyList = new ArrayList<>();
        when(userHistoryTrendingService.retrieveTrendingUserSearches(anyInt())).thenReturn(historyList);

        ResponseEntity<List<UserHistory>> response = userHistoryController
                .retrieveTrendingUserSearches("10");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteIndex() throws IOException {
        UserHistoryController userHistoryController = new UserHistoryController(
                dateService,
                userHistoryService,
                userHistoryPopularService,
                userHistoryTrendingService,
                userHistoryIngestionService
        );

        DeleteIndexResponse deleteIndexResponse = Mockito.mock(DeleteIndexResponse.class);
        when(userHistoryService.deleteIndex(anyString())).thenReturn(deleteIndexResponse);

        ResponseEntity<DeleteIndexResponse> response = userHistoryController
                .deleteIndex("nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteHitCountDocument() throws IOException {
        UserHistoryController userHistoryController = new UserHistoryController(
                dateService,
                userHistoryService,
                userHistoryPopularService,
                userHistoryTrendingService,
                userHistoryIngestionService
        );

        DeleteResponse deleteResponse = Mockito.mock(DeleteResponse.class);
        when(userHistoryService.deleteDocument(anyString(), anyString())).thenReturn(deleteResponse);

        ResponseEntity<DeleteResponse> response = userHistoryController
                .deleteHitCountDocument("hit_count", "nl88888");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

}
