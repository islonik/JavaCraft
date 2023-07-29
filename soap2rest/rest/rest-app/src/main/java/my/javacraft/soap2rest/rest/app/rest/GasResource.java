package my.javacraft.soap2rest.rest.app.rest;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.MeterDao;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.Meter;
import my.javacraft.soap2rest.rest.app.service.GasService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/smart")
public class GasResource {

    @Autowired
    private MeterDao meterDao;

    @Autowired
    private GasMetricDao gasMetricDao;

    @Autowired
    private GasService gasService;

    @ExecutionTime
    @GetMapping(value = "/{id}/gas",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GasMetric>> getGasMetrics(@PathVariable Long id) {
        List<Meter> meterList = meterDao.findByAccountId(id);

        return ResponseEntity
                .ok(gasMetricDao.findByMeterIds(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                ));
    }

    @ExecutionTime
    @GetMapping(value = "/{id}/gas/latest",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GasMetric> getLatestGasMetric(@PathVariable Long id) {
        List<Meter> meterList = meterDao.findByAccountId(id);

        return ResponseEntity
                .ok(gasMetricDao.findTopByMeterIdInOrderByDateDesc(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                ));
    }

    @ExecutionTime
    @PutMapping(value = "/{id}/gas",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GasMetric> putNewGasMetric(
            @RequestBody GasMetric gasMetric) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(gasService.submit(gasMetric));
    }

    @ExecutionTime
    @DeleteMapping(value = "/{id}/gas",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteAllGasMetrics() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(gasService.deleteAll());
    }
}
