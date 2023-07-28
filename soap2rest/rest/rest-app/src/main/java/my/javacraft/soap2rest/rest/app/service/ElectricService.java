package my.javacraft.soap2rest.rest.app.service;

import java.util.Collections;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectricService {

    @Autowired
    private MetricValidationService metricValidationService;

    @Autowired
    private ElectricMetricDao electricMetricDao;

    public ElectricMetric submit(Metric submittedMetric) {
        ElectricMetric latestMetric = electricMetricDao
                .findTopByMeterIdInOrderByDateDesc(
                        Collections.singletonList(submittedMetric.getMeterId())
                );

        metricValidationService.validate(latestMetric, submittedMetric);

        ElectricMetric electricMetric = new ElectricMetric();
        electricMetric.setMeterId(submittedMetric.getMeterId());
        electricMetric.setReading(submittedMetric.getReading());
        electricMetric.setDate(submittedMetric.getDate());

        electricMetricDao.save(electricMetric);

        return electricMetric;
    }
}
