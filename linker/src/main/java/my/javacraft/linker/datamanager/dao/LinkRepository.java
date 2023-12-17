package my.javacraft.linker.datamanager.dao;

import my.javacraft.linker.datamanager.dao.entity.Link;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface LinkRepository extends MongoRepository<Link, String> {

    @Query("{shortUrl:'?0'}")
    Link findLinkByShortUrl(String shortUrl);

}
