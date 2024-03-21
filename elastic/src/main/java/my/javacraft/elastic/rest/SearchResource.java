package my.javacraft.elastic.rest;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.SeekRequest;
import my.javacraft.elastic.service.SearchService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.elasticsearch.core.document.Document;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "1. Search", description = "API(s) for search services")
@RequestMapping(path = "/api/services/search")
@RequiredArgsConstructor
public class SearchResource {

    final SearchService searchService;

    @Operation(
            summary = "Wildcard search request",
            description = "API to make a wildcard search request."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @PostMapping(
            value = "/wildcard",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> wildcardSearch(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Wildcard search request values",
                    useParameterTypeSchema = true,
                    content = @Content(schema = @Schema(
                            implementation = SeekRequest.class
                    ))
            )
            @RequestBody @Valid SeekRequest seekRequest) throws IOException, ElasticsearchException {

        log.info("searching wildcard for (SearchRequest = {})...", seekRequest);

        List<Object> documentList = searchService.wildcardSearch(seekRequest);

        return ResponseEntity.ok().body(documentList);
    }

    @Operation(
            summary = "Fuzzy search request",
            description = "API to make a fuzzy search request."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @PostMapping(
            value = "/fuzzy",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> fuzzySearch(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Fuzzy search request values",
                    useParameterTypeSchema = true,
                    content = @Content(schema = @Schema(
                            implementation = SeekRequest.class
                    ))
            )
            @RequestBody @Valid SeekRequest seekRequest) throws IOException, ElasticsearchException {

        log.info("searching fuzzy for (SearchRequest = {})...", seekRequest);

        List<Object> documentList = searchService.fuzzySearch(seekRequest);

        return ResponseEntity.ok().body(documentList);
    }

    @Operation(
            summary = "Span search request",
            description = "API to make a span search request."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @PostMapping(
            value = "/span",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object>> spanSearch(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Span search request values",
                    useParameterTypeSchema = true,
                    content = @Content(schema = @Schema(
                            implementation = SeekRequest.class
                    ))
            )
            @RequestBody @Valid SeekRequest seekRequest) throws IOException, ElasticsearchException {

        log.info("searching span for (SearchRequest = {})...", seekRequest);

        List<Object> documentList = searchService.spanQuery(seekRequest);

        return ResponseEntity.ok().body(documentList);
    }

    @Operation(
            summary = "Search request",
            description = "API to make a search request."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "406", description = "Resource unavailable")
    })
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Document>> search(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Search request values",
                    useParameterTypeSchema = true,
                    content = @Content(schema = @Schema(
                            implementation = SeekRequest.class
                    ))
            )
            @RequestBody @Valid SeekRequest seekRequest) throws IOException, ElasticsearchException {

        log.info("searching (SearchRequest = {})...", seekRequest);

        List<Document> documentList = searchService.search(seekRequest);

        return ResponseEntity.ok().body(documentList);
    }
}
