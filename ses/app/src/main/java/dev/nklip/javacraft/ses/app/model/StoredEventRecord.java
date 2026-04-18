package dev.nklip.javacraft.ses.app.model;

import dev.nklip.javacraft.ses.events.Event;

public record StoredEventRecord(
        long storeId,
        Event event
) {
}
