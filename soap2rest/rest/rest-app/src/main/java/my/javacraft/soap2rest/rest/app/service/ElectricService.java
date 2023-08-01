package my.javacraft.soap2rest.rest.app.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.MetricEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectricService {

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricValidationService metricValidationService;

    @Autowired
    private ElectricMetricDao electricMetricDao;

    public List<Metric> findMetrics(Long accountId) {
        return metricService.calculateExtraFields(electricMetricDao
                .findByAccountId(accountId)
                .stream()
                .map(MetricEntity::toApiMetric)
                .toList());
    }

    public Metric findLatestMetric(Long accountId) {
        return Optional.of(findMetrics(accountId))
                .filter(l -> !l.isEmpty())
                .map(l -> l.get(l.size() - 1))
                .orElse(null);
    }

    public Metric submit(Metric submittedMetric) {
        Metric latestMetric = Optional.ofNullable(electricMetricDao
                .findTopByMeterIdInOrderByDateDesc(
                        Collections.singletonList(submittedMetric.getMeterId())
                )).map(MetricEntity::toApiMetric).orElse(null);

        metricValidationService.validate(latestMetric, submittedMetric);

        ElectricMetric electricMetric = new ElectricMetric();
        electricMetric.setMeterId(submittedMetric.getMeterId());
        electricMetric.setReading(submittedMetric.getReading());
        electricMetric.setDate(submittedMetric.getDate());

        electricMetricDao.save(electricMetric);

        return electricMetric.toApiMetric();
    }

    public boolean deleteAll() {
        electricMetricDao.deleteAll();
        return true;
    }
}
