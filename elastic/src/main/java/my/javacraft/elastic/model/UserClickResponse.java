package my.javacraft.elastic.model;

import co.elastic.clients.elasticsearch._types.Result;
import lombok.Data;

@Data
public class UserClickResponse {

    private String documentId;

    private Result result;

}
