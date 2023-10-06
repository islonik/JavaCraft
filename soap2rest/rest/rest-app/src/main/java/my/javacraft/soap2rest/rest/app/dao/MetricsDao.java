package my.javacraft.soap2rest.rest.app.dao;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.rest.app.service.ElectricService;
import my.javacraft.soap2rest.rest.app.service.GasService;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MetricsDao {

    private final ElectricService electricService;
    private final GasService gasService;

    public Metrics findByAccountId(Long accountId) {
        Metrics metrics = new Metrics();
        metrics.setAccountId(accountId);
        metrics.setElecReadings(electricService.getMetricsByAccountId(accountId));
        metrics.setGasReadings(gasService.getMetricsByAccountId(accountId));

        return metrics;
    }

    public Metrics findLatestMetrics(Long accountId) {
        Metrics metrics = new Metrics();
        metrics.setAccountId(accountId);
        metrics.setElecReadings(Collections.singletonList(electricService.findLatestMetric(accountId)));
        metrics.setGasReadings(Collections.singletonList(gasService.findLatestMetric(accountId)));

        return metrics;
    }

}
