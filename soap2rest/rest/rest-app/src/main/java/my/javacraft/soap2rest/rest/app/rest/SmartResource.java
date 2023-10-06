package my.javacraft.soap2rest.rest.app.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.rest.app.dao.MetricsDao;
import my.javacraft.soap2rest.rest.app.service.SmartService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/smart/{id}")
@RequiredArgsConstructor
public class SmartResource {

    private final MetricsDao metricsDao;
    private final SmartService smartService;

    @ExecutionTime
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metrics> getMetrics(@PathVariable Long id) {
        return ResponseEntity
                .ok(metricsDao.findByAccountId(id));
    }

    @ExecutionTime
    @GetMapping(value = "/latest",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metrics> getLatestMetrics(@PathVariable Long id) {
        return ResponseEntity
                .ok(metricsDao.findLatestMetrics(id));
    }

    @ExecutionTime
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> putMetrics(@RequestBody Metrics metrics) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(smartService.submit(metrics));
    }

    @ExecutionTime
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteAllMetrics() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(smartService.deleteAll());
    }

}
