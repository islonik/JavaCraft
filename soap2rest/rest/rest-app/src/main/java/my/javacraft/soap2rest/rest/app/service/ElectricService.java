package my.javacraft.soap2rest.rest.app.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.MetricEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElectricService {

    private final MetricService metricService;
    private final MetricValidationService metricValidationService;
    private final ElectricMetricDao electricMetricDao;

    public List<Metric> getMetricsByAccountId(Long accountId) {
        return metricService.calculateExtraFields(electricMetricDao.findMetrics(accountId));
    }

    public Metric findLatestMetric(Long accountId) {
        return Optional.of(getMetricsByAccountId(accountId))
                .filter(l -> !l.isEmpty())
                .map(List::getLast)
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
