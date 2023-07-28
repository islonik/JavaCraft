package my.javacraft.soap2rest.rest.app.dao.entity;

import java.util.List;
import lombok.Data;

@Data
public class Metrics {

    private Long accountId;

    private List<GasMetric> gasReadings;

    private List<ElectricMetric> electricReadings;
}
