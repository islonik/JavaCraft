package my.javacraft.soap2rest.rest.app.service;

import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.api.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmartService {

    @Autowired
    private GasService gasService;

    @Autowired
    private ElectricService electricService;

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
