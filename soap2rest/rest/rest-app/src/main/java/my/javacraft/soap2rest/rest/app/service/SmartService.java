package my.javacraft.soap2rest.rest.app.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.api.Metrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SmartService {

    private final GasService gasService;
    private final ElectricService electricService;

    @Transactional
    public boolean submit(Metrics metrics) {
        List<Metric> gasMetricList = metrics.getGasReadings();
        List<Metric> electricMetricList = metrics.getElecReadings();

        for (Metric gasMetric : gasMetricList) {
            gasService.submit(gasMetric);
        }
        for (Metric electricMetric : electricMetricList) {
            electricService.submit(electricMetric);
        }
        return true;
    }

    @Transactional
    public boolean deleteAll() {
        return gasService.deleteAll() && electricService.deleteAll();
    }
}
