package my.javacraft.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    ElasticsearchClient esClient;

    @Test
    public void testCreateUserHistoryIndex() throws IOException {
        AdminService adminService = new AdminService(esClient);

        ElasticsearchIndicesClient indicesClient = Mockito.mock(ElasticsearchIndicesClient.class);
        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);

        when(esClient.indices()).thenReturn(indicesClient);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        CreateIndexResponse response = adminService.createUserHistoryIndex();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.acknowledged());
    }

    @Test
    public void testCreateBooksIndex() throws IOException {
        AdminService adminService = new AdminService(esClient);

        ElasticsearchIndicesClient indicesClient = Mockito.mock(ElasticsearchIndicesClient.class);
        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);

        when(esClient.indices()).thenReturn(indicesClient);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        CreateIndexResponse response = adminService.createBooksIndex();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.acknowledged());
    }

    @Test
    public void testCreateMoviesIndex() throws IOException {
        AdminService adminService = new AdminService(esClient);

        ElasticsearchIndicesClient indicesClient = Mockito.mock(ElasticsearchIndicesClient.class);
        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);

        when(esClient.indices()).thenReturn(indicesClient);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        CreateIndexResponse response = adminService.createMoviesIndex();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.acknowledged());
    }

    @Test
    public void testCreateMusicIndex() throws IOException {
        AdminService adminService = new AdminService(esClient);

        ElasticsearchIndicesClient indicesClient = Mockito.mock(ElasticsearchIndicesClient.class);
        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);

        when(esClient.indices()).thenReturn(indicesClient);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createIndexResponse);
        when(createIndexResponse.acknowledged()).thenReturn(true);

        CreateIndexResponse response = adminService.createMusicIndex();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.acknowledged());
    }
}
