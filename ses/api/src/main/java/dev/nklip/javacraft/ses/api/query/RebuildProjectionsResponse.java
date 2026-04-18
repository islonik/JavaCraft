package dev.nklip.javacraft.ses.api.query;

/**
 * Result payload for an administrative projection rebuild.
 * Exists in {@code ses-api} to make replay diagnostics a stable contract instead of a controller-local detail.
 */
public record RebuildProjectionsResponse(
        long eventsReplayed,
        long requestsProjected,
        long budgetsProjected,
        long durationMillis
) {
}
