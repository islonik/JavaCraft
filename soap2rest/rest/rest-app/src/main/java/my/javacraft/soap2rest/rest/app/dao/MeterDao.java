package my.javacraft.soap2rest.rest.app.dao;

import java.util.List;
import my.javacraft.soap2rest.rest.app.dao.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MeterDao extends JpaRepository<Meter, Long> {
    @Query(value =
            "SELECT m FROM Meter m WHERE m.accountId = :id")
    List<Meter> findByAccountId(@Param("id") Long id);
}
