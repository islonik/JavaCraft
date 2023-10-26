package my.javacraft.linker.datamanager.dao.entity;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("link")
@Data
public class Link {

    @Id
    private String id;
    private String url;
    private String shortUrl;
    private Date creationDate;

}
