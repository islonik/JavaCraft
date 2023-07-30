package my.javacraft.soap2rest.rest.app.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.MeterDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.Meter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectricService {

    @Autowired
    private MeterDao meterDao;

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricValidationService metricValidationService;

    @Autowired
    private ElectricMetricDao electricMetricDao;

    public List<Metric> findMetrics(Long accountId) {
        List<Meter> meterList = meterDao.findByAccountId(accountId);

        return metricService.calculateExtraFields(
                electricMetricDao.findByMeterIds(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                ).stream().map(ElectricMetric::toApiMetric).toList()
        );
    }

    public Metric findLatestMetric(Long accountId) {
        List<Meter> meterList = meterDao.findByAccountId(accountId);

        return Optional.ofNullable(
                electricMetricDao.findTopByMeterIdInOrderByDateDesc(meterList
                        .stream()
                        .map(Meter::getId)
                        .toList()
                )).map(ElectricMetric::toApiMetric).orElse(null);
    }

    public Metric submit(Metric submittedMetric) {
        Metric latestMetric = Optional.ofNullable(electricMetricDao
                .findTopByMeterIdInOrderByDateDesc(
                        Collections.singletonList(submittedMetric.getMeterId())
                )).map(ElectricMetric::toApiMetric).orElse(null);

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
