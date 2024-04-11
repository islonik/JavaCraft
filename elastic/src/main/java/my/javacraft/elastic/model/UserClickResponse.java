package my.javacraft.elastic.model;

import co.elastic.clients.elasticsearch._types.Result;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserClickResponse {

    private String documentId;

    private Result result;

}
