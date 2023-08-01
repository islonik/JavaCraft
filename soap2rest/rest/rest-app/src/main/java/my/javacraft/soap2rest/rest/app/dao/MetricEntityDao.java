package my.javacraft.soap2rest.rest.app.dao;

import java.util.List;
import my.javacraft.soap2rest.rest.app.dao.entity.MetricEntity;

public interface MetricEntityDao {

    List<MetricEntity> findByAccountId(Long id);

    List<MetricEntity> findByMeterIds(List<Long> ids);

    MetricEntity findTopByMeterIdInOrderByDateDesc(List<Long> ids);
}
