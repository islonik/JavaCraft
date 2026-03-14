package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import java.io.IOException;
import my.javacraft.elastic.service.AdminService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    AdminService adminService;

    @Test
    public void testCreateUserHistoryIndex() throws IOException {
        AdminController adminController = new AdminController(adminService);

        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);
        when(adminService.createUserHistoryIndex()).thenReturn(createIndexResponse);

        ResponseEntity<CreateIndexResponse> response = adminController.createUserHistoryIndex();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testCreateBooksIndex() throws IOException {
        AdminController adminController = new AdminController(adminService);

        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);
        when(adminService.createBooksIndex()).thenReturn(createIndexResponse);

        ResponseEntity<CreateIndexResponse> response = adminController.createBooksIndex();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testCreateMoviesIndex() throws IOException {
        AdminController adminController = new AdminController(adminService);

        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);
        when(adminService.createMoviesIndex()).thenReturn(createIndexResponse);

        ResponseEntity<CreateIndexResponse> response = adminController.createMoviesIndex();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void testCreateMusicIndex() throws IOException {
        AdminController adminController = new AdminController(adminService);

        CreateIndexResponse createIndexResponse = Mockito.mock(CreateIndexResponse.class);
        when(adminService.createMusicIndex()).thenReturn(createIndexResponse);

        ResponseEntity<CreateIndexResponse> response = adminController.createMusicIndex();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
    }
}
