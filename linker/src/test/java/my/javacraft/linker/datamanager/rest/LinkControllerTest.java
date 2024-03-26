package my.javacraft.linker.datamanager.rest;

import java.util.ArrayList;
import java.util.List;
import my.javacraft.linker.datamanager.dao.LinkRepository;
import my.javacraft.linker.datamanager.dao.entity.Link;
import my.javacraft.linker.datamanager.service.LinkServices;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class LinkControllerTest {

    LinkRepository linkRepository;
    LinkServices linkServices;
    LinkController linkController;

    @BeforeEach
    public void setUp() {
        linkRepository = Mockito.mock(LinkRepository.class);
        linkServices = new LinkServices(linkRepository);
        linkController = new LinkController(linkRepository, linkServices);
    }

    @Test
    public void testFindAll() {
        List<Link> exptectedList = new ArrayList<>();
        Link link = new Link();
        link.setId("112");
        link.setUrl("long-url");
        link.setShortUrl("short-url");
        exptectedList.add(link);

        Mockito.when(linkRepository.findAll()).thenReturn(exptectedList);

        ResponseEntity<List<Link>> actualList = linkController.findAll();

        Assertions.assertNotNull(actualList);
        Assertions.assertNotNull(actualList.getBody());
        Assertions.assertEquals(1, actualList.getBody().size());
        Assertions.assertEquals("112", actualList.getBody().getFirst().getId());
    }

    @Test
    public void testShortUrl2FullUrl() {
        Link link = new Link();
        link.setId("112");
        link.setUrl("long-url");
        link.setShortUrl("short-url");

        Mockito.when(linkRepository.findLinkByShortUrl(Mockito.eq("short-url"))).thenReturn(link);

        ResponseEntity<byte[]> redirectResponse = linkController.shortUrl2FullUrl("short-url");

        Assertions.assertNotNull(redirectResponse);
        Assertions.assertNotNull(redirectResponse.getStatusCode());
        Assertions.assertEquals(302, redirectResponse.getStatusCode().value());
        Assertions.assertTrue(redirectResponse.getStatusCode().is3xxRedirection());

        Assertions.assertNotNull(redirectResponse.getHeaders());
        Assertions.assertEquals(1, redirectResponse.getHeaders().size());
        List<String> locationList = redirectResponse.getHeaders().get(HttpHeaders.LOCATION);
        Assertions.assertNotNull(locationList);
        Assertions.assertEquals(1, locationList.size());
        Assertions.assertEquals("long-url", locationList.getFirst());
    }

    @Test
    public void testAddLink() {
        Link link = new Link();
        link.setId("112");
        link.setUrl("long-url");
        link.setShortUrl("short-url");

        linkServices.setHost("http://localhost:8080/");
        Mockito.when(linkRepository.save(Mockito.any())).thenReturn(link);

        ResponseEntity<String> response = linkController.addLink("long-url");

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("http://localhost:8080/short-url", response.getBody());
    }


}
