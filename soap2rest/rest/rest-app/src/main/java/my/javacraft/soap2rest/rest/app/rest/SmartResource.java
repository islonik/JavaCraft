package my.javacraft.soap2rest.rest.app.rest;

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

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/smart")
@RequiredArgsConstructor
public class SmartResource {

    private final MetricsDao metricsDao;
    private final SmartService smartService;
    @Value("${soap2rest.rest.smart.message:Hello World!}")
    private String smartMessage;

    @ExecutionTime
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDefault() {
        return ResponseEntity.ok(smartMessage);
    }

    @ExecutionTime
    @GetMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metrics> getMetrics(@PathVariable Long id) {
        return ResponseEntity
                .ok(metricsDao.findByAccountId(id));
    }

    @ExecutionTime
    @GetMapping(value = "/{id}/latest",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metrics> getLatestMetrics(@PathVariable Long id) {
        return ResponseEntity
                .ok(metricsDao.findLatestMetrics(id));
    }

    @ExecutionTime
    @PutMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> putMetrics(@RequestBody Metrics metrics) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(smartService.submit(metrics));
    }

    @ExecutionTime
    @DeleteMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteAllMetrics() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(smartService.deleteAll());
    }

}
