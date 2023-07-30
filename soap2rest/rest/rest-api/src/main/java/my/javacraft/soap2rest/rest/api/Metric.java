package my.javacraft.soap2rest.rest.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.sql.Date;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric {

    private Long id;

    private Long meterId;

    private BigDecimal reading;

    private Date date;

    private BigDecimal usageSinceLastRead;

    private Long periodSinceLastRead;

}
