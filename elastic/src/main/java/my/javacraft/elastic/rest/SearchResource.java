package my.javacraft.elastic.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.model.SearchRequest;
import my.javacraft.elastic.service.SearchService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.elasticsearch.core.document.Document;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@Tag(name = "Search", description = "List of APIs for search services")
@RequestMapping(path = "/api/services/search")
@RequiredArgsConstructor
public class SearchResource {

    final SearchService searchService;

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
                            implementation = SearchRequest.class
                    ))
            )
            @RequestBody @Valid SearchRequest searchRequest)  {

        log.info("searching (SearchRequest = {})...", searchRequest);

        List<Document> documentList = null; // TODO: implement it

        return ResponseEntity.ok().body(documentList);
    }
}
