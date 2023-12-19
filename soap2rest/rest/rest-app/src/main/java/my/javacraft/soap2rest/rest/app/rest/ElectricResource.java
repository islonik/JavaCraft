package my.javacraft.soap2rest.rest.app.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.service.ElectricService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "Electric", description = "List of APIs for electric metrics")
@RequestMapping(path = "/api/v1/smart/{id}/electric")
@RequiredArgsConstructor
public class ElectricResource {

    private final ElectricService electricService;

    @ExecutionTime
    @Operation(
            summary = "Get electric metrics",
            description = "API to get electric metrics"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Metric>> getElectricMetrics(@PathVariable("id") Long id) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(electricService.getMetricsByAccountId(id));
    }

    @ExecutionTime
    @Operation(
            summary = "Get the LATEST electric metric",
            description = "API to get the LATEST electric metric"
    )
    @GetMapping(value = "/latest",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metric> getLatestElectricMetric(@PathVariable("id") Long id) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(electricService.findLatestMetric(id));
    }

    @ExecutionTime
    @Operation(
            summary = "Add a new electric metric",
            description = "API to add a new electric metric"
    )
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metric> putNewElectricMetric(
            @RequestBody Metric metric) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(electricService.submit(metric));
    }

    @ExecutionTime
    @Operation(
            summary = "Delete all electric metrics",
            description = "API to delete all electric metrics"
    )
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteAllElectricMetrics() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(electricService.deleteAll());
    }

}
