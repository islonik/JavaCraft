package dev.nklip.javacraft.ses.api.query;

import dev.nklip.javacraft.ses.events.EventStatus;
import dev.nklip.javacraft.ses.events.Priority;
import java.time.Instant;
import java.util.UUID;

/**
 * Projected snapshot of a work request returned by command and query endpoints.
 * Exists in {@code ses-api} so read models can be shared by REST controllers, SSE payloads, and black-box tests.
 */
public record WorkRequestResponse(
        int requestId,
        String title,
        Priority priority,
        String budgetCode,
        int estimate,
        EventStatus status,
        String requestedBy,
        String lastActor,
        String reason,
        UUID lastEventId,
        Instant lastOccurredAt,
        long streamVersion
) {
}
