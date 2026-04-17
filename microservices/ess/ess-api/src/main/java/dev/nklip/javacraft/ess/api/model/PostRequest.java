package dev.nklip.javacraft.ess.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import dev.nklip.javacraft.ess.api.config.ApiLimits;

// Represents the incoming request for PostService#submitPost
@Data
@ToString
public class PostRequest {

    @NotBlank
    @Size(max = ApiLimits.MAX_USER_ID_LENGTH)
    private String authorUserId;
}
