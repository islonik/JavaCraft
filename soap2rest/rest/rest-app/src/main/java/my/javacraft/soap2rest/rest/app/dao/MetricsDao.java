package my.javacraft.soap2rest.rest.app.dao;

import java.util.Collections;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.rest.app.service.ElectricService;
import my.javacraft.soap2rest.rest.app.service.GasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MetricsDao {

    @Autowired
    private ElectricService electricService;

    @Autowired
    private GasService gasService;

    public Metrics findByAccountId(Long accountId) {
        Metrics metrics = new Metrics();
        metrics.setAccountId(accountId);
        metrics.setElectricReadings(electricService.findMetrics(accountId));
        metrics.setGasReadings(gasService.findMetrics(accountId));

        return metrics;
    }

    public Metrics findLatestMetrics(Long accountId) {
        Metrics metrics = new Metrics();
        metrics.setAccountId(accountId);
        metrics.setElectricReadings(Collections.singletonList(electricService.findLatestMetric(accountId)));
        metrics.setGasReadings(Collections.singletonList(gasService.findLatestMetric(accountId)));

        return metrics;
    }

}
