package my.javacraft.soap2rest.rest.app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by nikilipa on 3/31/17.
 */
public class MessageDao {

    @PersistenceContext(unitName = "my.javacraft.soap2rest.rest")
    private EntityManager em;

    public EntityManager em() {
        return em;
    }

    public Message findById(Long id) {
        return em.createNamedQuery("findById", Message.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<Message> findAll() {
        return em.createNamedQuery("findAll", Message.class)
                .getResultList();
    }

}