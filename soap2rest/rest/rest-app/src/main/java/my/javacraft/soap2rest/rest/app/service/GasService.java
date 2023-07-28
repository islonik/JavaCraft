package my.javacraft.soap2rest.rest.app.service;

import java.util.Collections;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GasService {

    @Autowired
    private MetricValidationService metricValidationService;

    @Autowired
    private GasMetricDao gasMetricDao;

    public GasMetric submit(Metric submittedMetric) {
        GasMetric latestGasMetric = gasMetricDao
                .findTopByMeterIdInOrderByDateDesc(
                        Collections.singletonList(submittedMetric.getMeterId())
                );

        metricValidationService.validate(latestGasMetric, submittedMetric);

        GasMetric gasMetric = new GasMetric();
        gasMetric.setMeterId(submittedMetric.getMeterId());
        gasMetric.setReading(submittedMetric.getReading());
        gasMetric.setDate(submittedMetric.getDate());

        gasMetricDao.save(gasMetric);

        return gasMetric;
    }
}
