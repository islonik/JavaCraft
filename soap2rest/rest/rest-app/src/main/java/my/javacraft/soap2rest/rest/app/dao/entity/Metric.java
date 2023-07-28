package my.javacraft.soap2rest.rest.app.dao.entity;

import java.math.BigDecimal;
import java.sql.Date;

public interface Metric {
    Long getId();

    void setId(Long id);

    Long getMeterId();

    void setMeterId(Long meterId);

    BigDecimal getReading();

    void setReading(BigDecimal reading);

    Date getDate();

    void setDate(Date date);
}
