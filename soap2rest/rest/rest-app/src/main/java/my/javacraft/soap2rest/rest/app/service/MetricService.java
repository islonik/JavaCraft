package my.javacraft.soap2rest.rest.app.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    public List<Metric> calculateExtraFields(List<Metric> metricList) {
        BigDecimal lastUsage = null;
        Date lastDate = null;
        for (Metric metric : metricList) {
            if (lastUsage != null) {
                metric.setUsageSinceLastRead(metric.getReading().subtract(lastUsage));
            }
            if (lastDate != null) {
                long differenceInDays = ChronoUnit.DAYS.between(
                        LocalDate.parse(lastDate.toString()),
                        LocalDate.parse(metric.getDate().toString())
                );
                metric.setPeriodSinceLastRead(differenceInDays);
            }
            lastUsage = metric.getReading();
            lastDate = metric.getDate();
        }
        return metricList;
    }
}
