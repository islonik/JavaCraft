package my.javacraft.soap2rest.rest.app.dao;

import java.util.List;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricMetricDao extends JpaRepository<ElectricMetric, Long> {

    @Query(value = """
            SELECT e FROM Account a, Meter m, ElectricMetric e 
            WHERE a.id = m.accountId AND m.id = e.meterId AND a.id = :id
            ORDER by e.date ASC
            """)
    List<ElectricMetric> findByAccountId(@Param("id") Long id);

    @Query(value = "SELECT e FROM ElectricMetric e WHERE e.meterId IN :ids ORDER by e.date ASC")
    List<ElectricMetric> findByMeterIds(@Param("ids") List<Long> ids);

    ElectricMetric findTopByMeterIdInOrderByDateDesc(List<Long> ids);
}
