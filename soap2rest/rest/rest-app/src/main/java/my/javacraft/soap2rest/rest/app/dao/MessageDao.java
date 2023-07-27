package my.javacraft.soap2rest.rest.app.dao;

import my.javacraft.soap2rest.rest.app.dao.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageDao extends JpaRepository<Message, Long> {

}