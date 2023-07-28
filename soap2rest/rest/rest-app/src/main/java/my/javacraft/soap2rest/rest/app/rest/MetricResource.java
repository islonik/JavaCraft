package my.javacraft.soap2rest.rest.app.rest;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.app.dao.MetricsDao;
import my.javacraft.soap2rest.rest.app.dao.entity.Metrics;
import my.javacraft.soap2rest.rest.app.service.MetricsService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/smart")
public class MetricResource {

    @Autowired
    private MetricsDao metricsDao;

    @Autowired
    private MetricsService metricsService;

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
    public ResponseEntity<Boolean> putMessage(@RequestBody Metrics metrics) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(metricsService.submit(metrics));
    }

}
