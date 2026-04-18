package dev.nklip.javacraft.ses.app.model;

import dev.nklip.javacraft.ses.events.Event;
import dev.nklip.javacraft.ses.events.EventStatus;
import dev.nklip.javacraft.ses.events.Priority;
import java.util.List;
import java.util.Objects;

public record WorkRequestAggregate(
        int taskId,
        String title,
        Priority priority,
        String budgetCode,
        int estimate,
        EventStatus status,
        String requestedBy,
        String lastActor,
        String reason,
        long streamVersion,
        boolean exists
) {

    public static WorkRequestAggregate missing(int taskId) {
        return new WorkRequestAggregate(taskId, null, null, null, 0, null, null, null, null, 0L, false);
    }

    public static WorkRequestAggregate rehydrate(int taskId, List<Event> history) {
        Objects.requireNonNull(history, "History must not be null");
        if (history.isEmpty()) {
            return missing(taskId);
        }

        Event createdEvent = history.getFirst();
        Event latestEvent = history.getLast();

        return new WorkRequestAggregate(
                taskId,
                latestEvent.getTitle(),
                latestEvent.getPriority(),
                latestEvent.getBudgetCode(),
                latestEvent.getEstimate(),
                latestEvent.getStatus(),
                createdEvent.getActor(),
                latestEvent.getActor(),
                latestEvent.getReason(),
                latestEvent.getStreamVersion(),
                true
        );
    }

    public boolean canApprove() {
        return exists && status == EventStatus.CREATED;
    }

    public boolean canReject() {
        return exists && status == EventStatus.CREATED;
    }

    public boolean canStart() {
        return exists && status == EventStatus.ACCEPTED;
    }

    public boolean canComplete() {
        return exists && status == EventStatus.RUNNING;
    }
}
