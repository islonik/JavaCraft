package my.javacraft.soap2rest.rest.app.dao.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import lombok.Data;
import my.javacraft.soap2rest.rest.api.Metric;

@Data
@Entity
@Table(name = "electric_metric")
public class ElectricMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meter_id")
    private Long meterId;

    @Column(name = "reading")
    private BigDecimal reading;

    @Column(name = "date")
    private Date date;

    public Metric toApiMetric() {
        Metric metric = new Metric();
        metric.setId(id);
        metric.setMeterId(meterId);
        metric.setReading(reading);
        metric.setDate(date);
        return metric;
    }

}
