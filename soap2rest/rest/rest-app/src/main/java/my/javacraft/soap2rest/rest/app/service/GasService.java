package my.javacraft.soap2rest.rest.app.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.MetricEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GasService {

    private final MetricService metricService;
    private final MetricValidationService metricValidationService;
    private final GasMetricDao gasMetricDao;

    public List<Metric> getMetricsByAccountId(Long accountId) {
        return metricService.calculateExtraFields(gasMetricDao.findMetrics(accountId));
    }

    public Metric findLatestMetric(Long accountId) {
        return Optional.of(getMetricsByAccountId(accountId))
                .filter(l -> !l.isEmpty())
                .map(l -> l.get(l.size() - 1))
                .orElse(null);
    }

    public Metric submit(Metric submittedMetric) {
        Metric latestMetric = Optional.ofNullable(gasMetricDao
                .findTopByMeterIdInOrderByDateDesc(
                        Collections.singletonList(submittedMetric.getMeterId())
                )).map(MetricEntity::toApiMetric).orElse(null);

        metricValidationService.validate(latestMetric, submittedMetric);

        GasMetric gasMetric = new GasMetric();
        gasMetric.setMeterId(submittedMetric.getMeterId());
        gasMetric.setReading(submittedMetric.getReading());
        gasMetric.setDate(submittedMetric.getDate());

        gasMetricDao.save(gasMetric);

        return gasMetric.toApiMetric();
    }

    public boolean deleteAll() {
        gasMetricDao.deleteAll();
        return true;
    }
}
