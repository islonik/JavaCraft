package my.javacraft.translation.resource;

import my.javacraft.translation.service.WordCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/words")
public class WordCounterResource {

    @Autowired
    private WordCounterService wordCounterService;

    @GetMapping(value = "/{word}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> counterByWord(@PathVariable String word) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(wordCounterService.counterByWord(word));
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putNewElectricMetric(
            @RequestBody String text) {
        wordCounterService.addWords(text);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("Processed.");
    }

}
