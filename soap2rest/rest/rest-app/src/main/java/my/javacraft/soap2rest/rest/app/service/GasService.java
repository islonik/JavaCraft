package my.javacraft.soap2rest.rest.app.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.MeterDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.Meter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GasService {

    @Autowired
    private MeterDao meterDao;

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricValidationService metricValidationService;

    @Autowired
    private GasMetricDao gasMetricDao;

    public List<Metric> findMetrics(Long accountId) {
        List<Meter> meterList = meterDao.findByAccountId(accountId);

        return metricService.calculateExtraFields(
                gasMetricDao.findByMeterIds(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                ).stream().map(GasMetric::toApiMetric).toList()
        );
    }

    public Metric findLatestMetric(Long accountId) {
        List<Meter> meterList = meterDao.findByAccountId(accountId);

        return Optional.ofNullable(
                gasMetricDao.findTopByMeterIdInOrderByDateDesc(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                )).map(GasMetric::toApiMetric).orElse(null);
    }

    public Metric submit(Metric submittedMetric) {
        Metric latestMetric = Optional.ofNullable(gasMetricDao
                .findTopByMeterIdInOrderByDateDesc(
                        Collections.singletonList(submittedMetric.getMeterId())
                )).map(GasMetric::toApiMetric).orElse(null);

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
