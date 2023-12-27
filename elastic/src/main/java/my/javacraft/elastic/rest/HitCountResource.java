package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.HitCount;
import my.javacraft.elastic.service.HitCountService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;


@SuppressWarnings({"rawtypes"})
@Slf4j
@RestController
@Tag(name = "HitCount", description = "List of APIs for hit count services")
@RequestMapping(path = "/api/services/hitCount")
@RequiredArgsConstructor
public class HitCountResource {

    private final HitCountService hitCountService;

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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "HitCount values",
                    useParameterTypeSchema = true,
                    content = @Content(schema = @Schema(
                            implementation = HitCount.class
                    ))
            )
            @RequestBody HitCount hitCount) throws IOException {

        log.info("executing capture (hitCount = {})...", hitCount);

        UpdateResponse updateResponse = hitCountService.capture(hitCount);

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
    @GetMapping("/document/{documentId}")
    public ResponseEntity<GetResponse<Map>> getHitCount(
            @PathVariable("documentId") String documentId) throws IOException {

        log.info("executing getHitCount (documentId = '{}')...", documentId);

        GetResponse<Map> map = hitCountService.getHitCount(documentId);

        return ResponseEntity.ok().body(map);
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
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map>> getSearchHistory(
            @PathVariable("userId") String userId) throws IOException {

        log.info("executing getSearchHistory (userId = '{}')...", userId);

        List<Map> mapList = hitCountService.searchHistoryByUserId(userId);

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
    @DeleteMapping("/index/{index}")
    public ResponseEntity<DeleteIndexResponse> deleteIndex(
            @PathVariable("index") String index) throws IOException {

        log.info("executing deleteIndex (index = '{}')...", index);

        DeleteIndexResponse deleteIndexResponse = hitCountService.deleteIndex(index);

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
    @DeleteMapping("/index/{index}/documentId/{documentId}")
    public ResponseEntity<DeleteResponse> deleteHitCountDocument(
            @PathVariable("index") String index,
            @PathVariable("documentId") String documentId) throws IOException {

        log.info("executing deleteHitCountDocument (index = '{}', documentId = '{}')...", index, documentId);

        DeleteResponse deleteResponse = hitCountService.deleteDocument(index, documentId);

        return ResponseEntity.ok()
                .body(deleteResponse);
    }

}
