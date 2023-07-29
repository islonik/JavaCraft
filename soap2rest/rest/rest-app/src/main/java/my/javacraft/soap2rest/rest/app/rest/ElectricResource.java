package my.javacraft.soap2rest.rest.app.rest;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.MeterDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.Meter;
import my.javacraft.soap2rest.rest.app.service.ElectricService;
import my.javacraft.soap2rest.utils.interceptor.ExecutionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/smart")
public class ElectricResource {

    @Autowired
    private MeterDao meterDao;

    @Autowired
    private ElectricMetricDao electricMetricDao;

    @Autowired
    private ElectricService electricService;

    @ExecutionTime
    @GetMapping(value = "/{id}/electric",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ElectricMetric>> getElectricMetrics(@PathVariable Long id) {
        List<Meter> meterList = meterDao.findByAccountId(id);

        return ResponseEntity
                .ok(electricMetricDao.findByAccountId(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                ));
    }

    @ExecutionTime
    @GetMapping(value = "/{id}/electric/latest",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ElectricMetric> getLatestElectricMetric(@PathVariable Long id) {
        List<Meter> meterList = meterDao.findByAccountId(id);

        return ResponseEntity
                .ok(electricMetricDao.findTopByMeterIdInOrderByDateDesc(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                ));
    }

    @ExecutionTime
    @PutMapping(value = "/{id}/electric",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ElectricMetric> putNewElectricMetric(
            @RequestBody ElectricMetric metric) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(electricService.submit(metric));
    }

    @ExecutionTime
    @DeleteMapping(value = "/{id}/electric",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteAllElectricMetrics() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(electricService.deleteAll());
    }

}
