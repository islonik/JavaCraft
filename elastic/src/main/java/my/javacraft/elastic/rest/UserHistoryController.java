package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
import my.javacraft.elastic.model.UserHistory;
import my.javacraft.elastic.service.DateService;
import my.javacraft.elastic.service.history.UserHistoryIngestionService;
import my.javacraft.elastic.service.history.UserHistoryPopularService;
import my.javacraft.elastic.service.history.UserHistoryService;
import my.javacraft.elastic.service.history.UserHistoryTrendingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@Tag(name = "2. User history", description = "API(s) for hit count services")
@RequestMapping(path = "/api/services/user-history")
@RequiredArgsConstructor
public class UserHistoryController {

    private final DateService dateService;
    private final UserHistoryService userHistoryService;
    private final UserHistoryPopularService userHistoryPopularService;
    private final UserHistoryTrendingService userHistoryTrendingService;
    private final UserHistoryIngestionService userHistoryIngestionService;

    @Operation(
            summary = "Capture user click",
            description = "Upsert - create a new hit count document or update(increment) the hit count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserClickResponse> captureUserClick(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User history values",
                    useParameterTypeSchema = true,
                    content = @Content(schema = @Schema(
                            implementation = UserClick.class
                    ))
            )
            @RequestBody @Valid UserClick userClick) throws IOException {

        log.info("ingesting (UserClick = {})...", userClick);

        UserClickResponse userClickResponse = userHistoryIngestionService.ingestUserClick(userClick, dateService.getCurrentDate());

        return ResponseEntity.ok().body(userClickResponse);
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
    @GetMapping(value = "/documents/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetResponse<UserHistory>> getHitCount(
            @PathVariable("documentId") String documentId) throws IOException {

        log.info("executing getHitCount (documentId = '{}')...", documentId);

        GetResponse<UserHistory> map = userHistoryService.getUserHistoryByDocumentId(documentId);

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
    @GetMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserHistory>> retrievePopularUserSearches(
            @PathVariable("userId") String userId,
            @RequestParam(required = false, name = "size", defaultValue = "10") String size) throws IOException {
        int limitSize = Integer.parseInt(size);

        log.info("retrieving popular user searches (userId = '{}' and limit = '{}')...", userId, limitSize);

        List<UserHistory> mapList = userHistoryPopularService.retrievePopularUserSearches(userId, limitSize);

        return ResponseEntity.ok().body(mapList);
    }

    @Operation(
            summary = "Fetch trending search services.",
            description = "Fetch trending search services."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserHistory>> retrieveTrendingUserSearches(
            @RequestParam(required = false, name = "size", defaultValue = "10") String size) throws IOException {
        int limitSize = Integer.parseInt(size);

        log.info("retrieving trending user searches (limit = '{}')...", limitSize);

        List<UserHistory> mapList = userHistoryTrendingService.retrieveTrendingUserSearches(limitSize);

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
    @DeleteMapping("/indexes/{index}")
    public ResponseEntity<DeleteIndexResponse> deleteIndex(
            @PathVariable("index") String index) throws IOException {

        log.info("executing deleteIndex (index = '{}')...", index);

        DeleteIndexResponse deleteIndexResponse = userHistoryService.deleteIndex(index);

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
    @DeleteMapping("/indexes/{index}/documents/{documentId}")
    public ResponseEntity<DeleteResponse> deleteHitCountDocument(
            @PathVariable("index") String index,
            @PathVariable("documentId") String documentId) throws IOException {

        log.info("executing deleteHitCountDocument (index = '{}', documentId = '{}')...", index, documentId);

        DeleteResponse deleteResponse = userHistoryService.deleteDocument(index, documentId);

        return ResponseEntity.ok()
                .body(deleteResponse);
    }

}
