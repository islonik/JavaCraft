package my.javacraft.elastic.model;

import lombok.Data;
import lombok.ToString;
import my.javacraft.elastic.validatiion.ValueOfEnum;

@Data
@ToString
public class SearchRequest {

    @ValueOfEnum(enumClass = SearchType.class)
    String type;

    String pattern;

    @ValueOfEnum(enumClass = Client.class)
    String client;

}
