package my.javacraft.elastic.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;
import my.javacraft.elastic.validatiion.ValueOfEnum;

// Represents the incoming event for UserHistoryService
@Data
@ToString
public class UserClick {
    @NotEmpty
    String userId;
    @NotBlank
    String documentId;
    @NotBlank
    String searchType;
    @NotBlank
    String searchPattern;


}
