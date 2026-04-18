package dev.nklip.javacraft.ses.api.command;

import dev.nklip.javacraft.ses.events.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Command payload for creating a new work request.
 * Exists in {@code ses-api} so HTTP clients, the Spring app, and test modules share one validated contract.
 */
public record CreateWorkRequest(
        @NotBlank String title,
        @NotNull Priority priority,
        @NotBlank String budgetCode,
        @Positive int estimate,
        @NotBlank String requestedBy,
        String correlationId
) {
}
