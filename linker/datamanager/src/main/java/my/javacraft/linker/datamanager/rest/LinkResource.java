package my.javacraft.linker.datamanager.rest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import my.javacraft.linker.datamanager.dao.LinkRepository;
import my.javacraft.linker.datamanager.dao.entity.Link;
import my.javacraft.linker.datamanager.service.LinkServices;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/links")
@RequiredArgsConstructor
public class LinkResource {

    private final LinkRepository linkRepository;
    private final LinkServices linkServices;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Link>> findAll() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkRepository.findAll());
    }

    //
    @GetMapping(value = "/{url}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> addUrl(@PathVariable String url) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkServices.createLink(url));
    }

}
