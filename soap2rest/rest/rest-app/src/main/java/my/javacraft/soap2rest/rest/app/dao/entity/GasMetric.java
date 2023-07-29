package my.javacraft.soap2rest.rest.app.dao.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import lombok.Data;

@Data
@Entity
@Table(name = "gas_metric")
public class GasMetric implements Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meter_id")
    private Long meterId;

    @Column(name = "reading")
    private BigDecimal reading;

    @Column(name = "date")
    private Date date;

}
