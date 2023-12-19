package my.javacraft.soap2rest.rest.app.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.rest.app.dao.MetricsDao;
import my.javacraft.soap2rest.rest.app.service.SmartService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Data
@Slf4j
@RestController
@Tag(name = "Smart", description = "List of APIs for smart metrics")
@RequestMapping(path = "/api/v1/smart")
@RequiredArgsConstructor
public class SmartResource {

    private final MetricsDao metricsDao;
    private final SmartService smartService;
    @Value("${soap2rest.rest.smart.message:Hello World!}")
    private String smartMessage;

    @ExecutionTime
    @Operation(
            summary = "Get default message",
            description = "API to get default message"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDefault() {
        return ResponseEntity.ok(smartMessage);
    }

    @ExecutionTime
    @Operation(
            summary = "Get metrics by account id",
            description = "API to get metrics by account id"
    )
    @GetMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metrics> getMetrics(@PathVariable Long id) {
        return ResponseEntity
                .ok(metricsDao.findByAccountId(id));
    }

    @ExecutionTime
    @Operation(
            summary = "Get the LATEST metric by account id",
            description = "API to get the LATEST metric by account id"
    )
    @GetMapping(value = "/{id}/latest",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metrics> getLatestMetrics(@PathVariable Long id) {
        return ResponseEntity
                .ok(metricsDao.findLatestMetrics(id));
    }

    @ExecutionTime
    @Operation(
            summary = "Create new metrics",
            description = "API to create new metrics"
    )
    @PutMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> putMetrics(@RequestBody Metrics metrics) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(smartService.submit(metrics));
    }

    @ExecutionTime
    @Operation(
            summary = "Delete all metrics",
            description = "API to delete all metrics"
    )
    @DeleteMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteAllMetrics() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(smartService.deleteAll());
    }

}
