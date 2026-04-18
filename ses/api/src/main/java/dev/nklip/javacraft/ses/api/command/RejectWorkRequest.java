package dev.nklip.javacraft.ses.api.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command payload for explicitly rejecting a work request.
 * Exists in {@code ses-api} because rejection semantics, including the operator reason, are part of the public write contract.
 */
public record RejectWorkRequest(
        @NotBlank String actor,
        @NotBlank String reason,
        String correlationId
) {
}
