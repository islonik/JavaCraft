package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.InlineScript;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("unchecked")
@Slf4j
@RestController
@Tag(name = "HitCount", description = "List of APIs for hit count services")
@RequestMapping(path = "/api/services/hitCount")
@RequiredArgsConstructor
public class HitCountResource {

    private final ElasticsearchClient esClient;

    @Operation(
            summary = "Capture hit count",
            description = "Upsert - create a new hit count document or update(increment) the hit count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @PostMapping(value = "/capture",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdateResponse> capture(
            @RequestBody Map<String, String> body) throws IOException {

        log.info("executing capture (body = {})...", body);

        String userId = body.get("userId");
        String documentId = body.get("documentId");
        String searchType = body.get("searchType");
        String searchPattern = body.get("searchPattern");

        InlineScript inlineScript = new InlineScript.Builder()
                .source("ctx._source.count++")
                .build();
        Script script = new Script.Builder()
                .inline(inlineScript)
                .build();

        UpdateRequest updateRequest = new UpdateRequest.Builder<>()
                .index("hit_count")
                .id(documentId)
                .script(script)
                .build();
        UpdateResponse<Map> updateResponse = esClient.update(updateRequest, Map.class);

        return ResponseEntity.ok().body(updateResponse);
    }

    @Operation(
            summary = "Search History by userId",
            description = "Fetch the search history by userId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<List<Map<String, String>>> getSearchHistory(
            @PathVariable("userId") String userId) throws IOException {

        log.info("executing getSearchHistory (userId = '{}')...", userId);

        SearchResponse<Map> search = esClient.search(s -> s
                        .index("hit_count")
                        .query(q -> q.term(t -> t
                                .field("userId")
                                .value(v -> v.stringValue(userId))
                        )),
                Map.class
        );

        List<Map<String, String>> mapList = new ArrayList<>();

        for (Hit<Map> hit: search.hits().hits()) {
            mapList.add(hit.source());
        }

        return ResponseEntity.ok().body(mapList);
    }

    @Operation(
            summary = "Delete index",
            description = "Delete index"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @DeleteMapping("/{index}")
    public ResponseEntity<DeleteIndexResponse> deleteIndex(
            @PathVariable("index") String index) throws IOException {

        log.info("executing deleteIndex (index = '{}')...", index);

        DeleteIndexRequest request = new DeleteIndexRequest.Builder().index(index).build();
        DeleteIndexResponse deleteIndexResponse = esClient.indices().delete(request);

        return ResponseEntity.ok()
                .body(deleteIndexResponse);
    }

    @Operation(
            summary = "Delete hit count document",
            description = "Delete hit count document"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @DeleteMapping("/{index}/{documentId}")
    public ResponseEntity<DeleteResponse> deleteHitCountDocument(
            @PathVariable("index") String index,
            @PathVariable("documentId") String documentId) throws IOException {

        log.info("executing deleteHitCountDocument (index = '{}', documentId = '{}')...", index, documentId);

        DeleteRequest request = new DeleteRequest.Builder().index(index).id(documentId).build();
        DeleteResponse deleteResponse = esClient.delete(request);

        return ResponseEntity.ok()
                .body(deleteResponse);
    }

}
