package my.javacraft.soap2rest.rest.app.service;

import java.util.List;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmartService {

    @Autowired
    private MetricValidationService metricValidationService;

    @Autowired
    private GasMetricDao gasMetricDao;

    @Autowired
    private ElectricMetricDao electricMetricDao;

    @Autowired
    private GasService gasService;

    @Autowired
    private ElectricService electricService;

    @Transactional
    public boolean submit(Metrics metrics) {
        List<GasMetric> gasMetricList = metrics.getGasReadings();
        List<ElectricMetric> electricMetricList = metrics.getElectricReadings();

        for (GasMetric gasMetric : gasMetricList) {
            electricService.submit(gasMetric);
        }
        for (ElectricMetric electricMetric : electricMetricList) {
            electricService.submit(electricMetric);
        }
        return true;
    }
}
