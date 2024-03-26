package my.javacraft.elastic.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import my.javacraft.elastic.model.*;
import my.javacraft.elastic.service.SearchService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchControllerTest {

    @Mock
    SearchService searchService;

    @Test
    public void testWildcardSearch() throws IOException {
        SearchController searchServiceController = new SearchController(searchService);

        Map<String, String> document = new LinkedHashMap<>();
        document.put("result", "test1 value");
        List<Object> documentList = new ArrayList<>();
        documentList.add(document);
        when(searchService.wildcardSearch(any(SeekRequest.class))).thenReturn(documentList);

        SeekRequest seekRequest = new SeekRequest();
        seekRequest.setClient(Client.WEB.toString());
        seekRequest.setType(SeekType.ALL.toString());
        seekRequest.setPattern("test1");

        ResponseEntity<List<Object>> response = searchServiceController.wildcardSearch(seekRequest);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isEmpty());
        Assertions.assertEquals(1, response.getBody().size());
    }

    @Test
    public void testFuzzySearch() throws IOException {
        SearchController searchServiceController = new SearchController(searchService);

        Map<String, String> document = new LinkedHashMap<>();
        document.put("result", "test2 value");
        List<Object> documentList = new ArrayList<>();
        documentList.add(document);
        when(searchService.fuzzySearch(any(SeekRequest.class))).thenReturn(documentList);

        SeekRequest seekRequest = new SeekRequest();
        seekRequest.setClient(Client.WEB.toString());
        seekRequest.setType(SeekType.ALL.toString());
        seekRequest.setPattern("tes?");

        ResponseEntity<List<Object>> response = searchServiceController.fuzzySearch(seekRequest);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isEmpty());
        Assertions.assertEquals(1, response.getBody().size());
    }

    @Test
    public void testSpanSearch() throws IOException {
        SearchController searchServiceController = new SearchController(searchService);

        Map<String, String> document = new LinkedHashMap<>();
        document.put("result", "test3 should be submitted");
        List<Object> documentList = new ArrayList<>();
        documentList.add(document);
        when(searchService.spanSearch(any(SeekRequest.class))).thenReturn(documentList);

        SeekRequest seekRequest = new SeekRequest();
        seekRequest.setClient(Client.WEB.toString());
        seekRequest.setType(SeekType.ALL.toString());
        seekRequest.setPattern("test3 submitted");

        ResponseEntity<List<Object>> response = searchServiceController.spanSearch(seekRequest);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isEmpty());
        Assertions.assertEquals(1, response.getBody().size());
    }

    @Test
    public void testSearch() throws IOException {
        SearchController searchServiceController = new SearchController(searchService);

        List<Document> documentList = new ArrayList<>();
        when(searchService.search(any(SeekRequest.class))).thenReturn(documentList);

        SeekRequest seekRequest = new SeekRequest();
        seekRequest.setClient(Client.WEB.toString());
        seekRequest.setType(SeekType.ALL.toString());
        seekRequest.setPattern("test4");

        ResponseEntity<List<Document>> response = searchServiceController.search(seekRequest);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().isEmpty());
    }

}
