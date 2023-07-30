package my.javacraft.soap2rest.rest.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.sql.Date;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric {

    /**
     * Metric unique id, i.e. a unique id for this record.
     */
    private Long id;

    /**
     * Meter unique id, i.e. a unique id for a physical meter.
     */
    private Long meterId;

    /**
     * Metric value equal to a value in DB, i.e. DECIMAL(20,3).
     */
    private BigDecimal reading;

    /**
     * Date in '2023-15-28' format
     */
    private Date date;

    /**
     Usage since the previous time.
     */
    private BigDecimal usageSinceLastRead;

    /**
     Days since the last reading time.
     */
    private Long periodSinceLastRead;

    /**
     Average daily usage taking into account all data.
     */
    private BigDecimal avgDailyUsage;

}
