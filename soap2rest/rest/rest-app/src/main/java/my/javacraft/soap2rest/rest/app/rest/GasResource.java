package my.javacraft.soap2rest.rest.app.rest;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.MeterDao;
import my.javacraft.soap2rest.rest.app.service.GasService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/smart/{id}/gas")
public class GasResource {

    @Autowired
    private MeterDao meterDao;

    @Autowired
    private GasMetricDao gasMetricDao;

    @Autowired
    private GasService gasService;

    @ExecutionTime
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Metric>> getGasMetrics(@PathVariable Long id) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(gasService.findMetrics(id));
    }

    @ExecutionTime
    @GetMapping(value = "/latest",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metric> getLatestGasMetric(@PathVariable Long id) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(gasService.findLatestMetric(id));
    }

    @ExecutionTime
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Metric> putNewGasMetric(
            @RequestBody Metric gasMetric) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(gasService.submit(gasMetric));
    }

    @ExecutionTime
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteAllGasMetrics() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(gasService.deleteAll());
    }
}
