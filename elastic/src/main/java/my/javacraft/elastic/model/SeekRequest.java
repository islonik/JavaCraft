package my.javacraft.elastic.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import my.javacraft.elastic.validation.ValueOfEnum;

@Data
@ToString
public class SeekRequest {

    @ValueOfEnum(enumClass = SeekType.class)
    String type;

    @NotBlank
    String pattern;

    @ValueOfEnum(enumClass = Client.class)
    String client;

}
