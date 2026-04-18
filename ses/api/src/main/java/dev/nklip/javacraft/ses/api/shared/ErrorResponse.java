package dev.nklip.javacraft.ses.api.shared;

import java.time.Instant;

/**
 * Standard error payload returned by SES HTTP endpoints.
 * Exists in {@code ses-api} so failures are described consistently to callers and reusable in end-to-end assertions.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
