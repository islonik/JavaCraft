package my.javacraft.elastic.service.history;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import java.io.IOException;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickTest;
import my.javacraft.elastic.service.DateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
public class UserHistoryIngestionServiceTest {

    @Mock
    ElasticsearchClient esClient;

    @Mock
    DateService dateService;

    @Test
    public void testCapture() throws IOException {
        UpdateResponse updateResponse = mock(UpdateResponse.class);
        when(dateService.getCurrentDate()).thenReturn("2024-01-15");
        when(esClient._jsonpMapper()).thenReturn(new JacksonJsonpMapper());
        when(esClient.update(any(UpdateRequest.class), any())).thenReturn(updateResponse);
        UserClick userClick = UserClickTest.createHitCount();

        UserHistoryIngestionService ingestionService = new UserHistoryIngestionService(esClient);
        Assertions.assertNotNull(ingestionService.ingestUserClick(userClick, dateService.getCurrentDate()));
    }

    @Test
    public void testCreateInlineScript() {
        UserHistoryIngestionService ingestionService = new UserHistoryIngestionService(esClient);
        Assertions.assertEquals("""
                        ctx._source.count++;
                        ctx._source.updated=params['updated'];
                        """,
                ingestionService.createInlineScript()
        );
    }
}
