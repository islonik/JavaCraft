# Linker
A simple keeper for short links and translation them into full links.

Tags: Spring Boot, MongoDb


### How to forward URL request to another request

```java
@RestController
@RequestMapping(path = "/api/v1/links")
@RequiredArgsConstructor
public class LinkResource {

    private final LinkRepository linkRepository;
    
    // redirection
    @GetMapping(value = "/{shortUrl}")
    public ResponseEntity<byte[]> shortUrl2FullUrl(@PathVariable String shortUrl) {
        String url = linkRepository.findLinkByShortUrl(shortUrl).getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, url);

        return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
    }
    
}
    
```
