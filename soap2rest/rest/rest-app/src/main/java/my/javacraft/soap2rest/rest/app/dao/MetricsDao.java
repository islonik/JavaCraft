package my.javacraft.soap2rest.rest.app.dao;

import java.util.Collections;
import java.util.List;
import my.javacraft.soap2rest.rest.app.dao.entity.Meter;
import my.javacraft.soap2rest.rest.app.dao.entity.Metrics;
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
        metrics.setElectricReadings(electricMetricDao.findByAccountId(meterIdList));
        metrics.setGasReadings(gasMetricDao.findByAccountId(meterIdList));

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
        metrics.setElectricReadings(Collections.singletonList(
                electricMetricDao.findTopByMeterIdInOrderByDateDesc(meterIdList)
        ));
        metrics.setGasReadings(Collections.singletonList(
                gasMetricDao.findTopByMeterIdInOrderByDateDesc(meterIdList)
        ));

        return metrics;
    }

}
