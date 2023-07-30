package my.javacraft.soap2rest.rest.app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    /**
     * Initial values should be sorted by date and values,
     *      i.e. each next value should have bigger value and bigger date.
     * @param metricList Metric list out of the DB with empty extra values.
     * @return Metric list with calculated values.
     */
    public List<Metric> calculateExtraFields(List<Metric> metricList) {
        BigDecimal initialValue = null;
        Date initialDate = null;

        BigDecimal lastUsage = null;
        Date lastDate = null;
        for (Metric metric : metricList) {
            if (lastUsage != null) {
                metric.setUsageSinceLastRead(metric.getReading().subtract(lastUsage));
            }
            if (lastDate != null) {
                metric.setPeriodSinceLastRead(differenceInDays(lastDate, metric.getDate()));
            }
            if (initialValue != null) {
                metric.setAvgDailyUsage(averageDailyUsage(initialValue, initialDate, metric));
            }

            // for next entities
            lastUsage = metric.getReading();
            lastDate = metric.getDate();

            // only for the first entity
            if (initialValue == null) {
                initialValue = metric.getReading();
                initialDate = metric.getDate();
            }
        }
        return metricList;
    }

    private Long differenceInDays(Date lastDate, Date currDate) {
        return ChronoUnit.DAYS.between(
                LocalDate.parse(lastDate.toString()),
                LocalDate.parse(currDate.toString())
        );
    }

    private BigDecimal averageDailyUsage(
            BigDecimal initialValue, Date initialDate, Metric metric) {

        BigDecimal absoluteValueDiff = metric.getReading().subtract(initialValue);
        Long absoluteDaysDiff = differenceInDays(initialDate, metric.getDate());

        return absoluteValueDiff.divide(
                new BigDecimal(absoluteDaysDiff),
                3, // three digits after decimal point
                RoundingMode.HALF_EVEN
        );

    }
}
