package my.javacraft.soap2rest.rest.app.dao;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.rest.app.dao.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MetricsDao {

    @Autowired
    private MeterDao meterDao;

    @Autowired
    private ElectricMetricDao electricMetricDao;
    @Autowired
    private GasMetricDao gasMetricDao;

    public Metrics findByAccountId(Long id) {
        List<Meter> meterList = meterDao.findByAccountId(id);

        List<Long> meterIdList = meterList
                .stream()
                .map(Meter::getId)
                .toList();

        Metrics metrics = new Metrics();
        metrics.setAccountId(id);
        metrics.setElectricReadings(electricMetricDao.findByMeterIds(meterIdList).stream().map(ElectricMetric::toApiMetric).toList());
        metrics.setGasReadings(gasMetricDao.findByMeterIds(meterIdList).stream().map(GasMetric::toApiMetric).toList());

        return metrics;
    }

    public Metrics findLatestMetrics(Long id) {
        List<Meter> meterList = meterDao.findByAccountId(id);

        List<Long> meterIdList = meterList
                .stream()
                .map(Meter::getId)
                .toList();

        Metrics metrics = new Metrics();
        metrics.setAccountId(id);
        metrics.setElectricReadings(Stream.of(
                electricMetricDao.findTopByMeterIdInOrderByDateDesc(meterIdList)
        ).map(ElectricMetric::toApiMetric).toList());
        metrics.setGasReadings(Stream.of(
                gasMetricDao.findTopByMeterIdInOrderByDateDesc(meterIdList)
        ).map(GasMetric::toApiMetric).toList());

        return metrics;
    }

}
