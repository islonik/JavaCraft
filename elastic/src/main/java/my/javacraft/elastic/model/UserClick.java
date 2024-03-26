package my.javacraft.elastic.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;

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
