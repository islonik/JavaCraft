package my.javacraft.elastic.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HitCount {
    String userId;
    String documentId;
    String searchType;
    String searchPattern;
}
