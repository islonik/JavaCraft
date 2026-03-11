package my.javacraft.linker.datamanager.rest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import my.javacraft.linker.datamanager.dao.LinkRepository;
import my.javacraft.linker.datamanager.dao.entity.Link;
import my.javacraft.linker.datamanager.service.LinkServices;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkRepository linkRepository;
    private final LinkServices linkServices;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Link>> findAll() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkRepository.findAll());
    }

    // redirection
    @GetMapping(value = "/{shortUrl}")
    public ResponseEntity<byte []> shortUrl2FullUrl(@PathVariable("shortUrl") String shortUrl) {
        LinkServices.ResolveLinkResult resolveLinkResult = linkServices.resolveLink(shortUrl);
        switch (resolveLinkResult.status()) {
            case NOT_FOUND -> {
                return ResponseEntity.notFound().build();
            }
            case EXPIRED -> {
                return ResponseEntity.status(HttpStatus.GONE).build();
            }
            case null, default -> {}
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, resolveLinkResult.url());

        return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
    }

    @GetMapping(value = "/{shortUrl}/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LinkServices.LinkAnalytics> findAnalytics(@PathVariable("shortUrl") String shortUrl) {
        return linkServices.getAnalytics(shortUrl)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // curl -i -X PUT -H "Content-Type: application/json" -d "https://mail.google.com/mail/u/0/inbox" http://localhost:8080/api/v1/links
    @PutMapping
    public ResponseEntity<String> addLink(@RequestBody String url) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkServices.createLink(url));
    }

}
