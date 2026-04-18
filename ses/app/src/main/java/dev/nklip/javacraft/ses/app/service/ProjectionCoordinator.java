package dev.nklip.javacraft.ses.app.service;

import dev.nklip.javacraft.ses.api.query.ProjectionUpdateResponse;
import dev.nklip.javacraft.ses.api.query.RebuildProjectionsResponse;
import dev.nklip.javacraft.ses.app.repository.BudgetProjectionRepository;
import dev.nklip.javacraft.ses.app.repository.ProjectionCheckpointRepository;
import dev.nklip.javacraft.ses.app.repository.WorkRequestProjectionRepository;
import dev.nklip.javacraft.ses.app.model.StoredEventRecord;
import dev.nklip.javacraft.ses.app.repository.EventStoreRepository;
import dev.nklip.javacraft.ses.events.EventNotifier;
import dev.nklip.javacraft.ses.events.EventsMonitor;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectionCoordinator {

    public static final String PROJECTION_NAME = "ses-projector";

    private static final Logger log = LoggerFactory.getLogger(ProjectionCoordinator.class);

    private final ProjectionApplier projectionApplier;
    private final ProjectionCheckpointRepository projectionCheckpointRepository;
    private final WorkRequestProjectionRepository workRequestProjectionRepository;
    private final BudgetProjectionRepository budgetProjectionRepository;
    private final EventStoreRepository eventStoreRepository;
    private final EventNotifier eventNotifier;
    private final EventsMonitor eventsMonitor;
    private final ProjectionSsePublisher projectionSsePublisher;
    private final Clock clock;
    private final ReentrantLock projectionLock = new ReentrantLock();

    public ProjectionCoordinator(
            ProjectionApplier projectionApplier,
            ProjectionCheckpointRepository projectionCheckpointRepository,
            WorkRequestProjectionRepository workRequestProjectionRepository,
            BudgetProjectionRepository budgetProjectionRepository,
            EventStoreRepository eventStoreRepository,
            EventNotifier eventNotifier,
            EventsMonitor eventsMonitor,
            ProjectionSsePublisher projectionSsePublisher,
            Clock sesClock
    ) {
        this.projectionApplier = projectionApplier;
        this.projectionCheckpointRepository = projectionCheckpointRepository;
        this.workRequestProjectionRepository = workRequestProjectionRepository;
        this.budgetProjectionRepository = budgetProjectionRepository;
        this.eventStoreRepository = eventStoreRepository;
        this.eventNotifier = eventNotifier;
        this.eventsMonitor = eventsMonitor;
        this.projectionSsePublisher = projectionSsePublisher;
        this.clock = sesClock;
    }

    public void catchUpFromLastApplied() {
        projectionLock.lock();
        try {
            long lastAppliedEventId = projectionCheckpointRepository.getLastAppliedEventId(PROJECTION_NAME);
            List<StoredEventRecord> unreadEvents = eventStoreRepository.findAfterStoreId(lastAppliedEventId);

            while (!unreadEvents.isEmpty()) {
                for (StoredEventRecord unreadEvent : unreadEvents) {
                    applyAndPublish(unreadEvent);
                    lastAppliedEventId = unreadEvent.storeId();
                }
                unreadEvents = eventStoreRepository.findAfterStoreId(lastAppliedEventId);
            }
        } finally {
            projectionLock.unlock();
        }
    }

    public RebuildProjectionsResponse rebuildProjections() {
        projectionLock.lock();
        try {
            Instant startedAt = Instant.now(clock);
            workRequestProjectionRepository.deleteAll();
            budgetProjectionRepository.resetFromReferenceData();
            projectionCheckpointRepository.reset(PROJECTION_NAME);
            eventsMonitor.clear();

            List<StoredEventRecord> history = eventStoreRepository.findAllOrdered();
            for (StoredEventRecord storedEventRecord : history) {
                applyAndPublish(storedEventRecord);
            }

            long durationMillis = Duration.between(startedAt, Instant.now(clock)).toMillis();
            return new RebuildProjectionsResponse(
                    history.size(),
                    workRequestProjectionRepository.count(),
                    budgetProjectionRepository.count(),
                    durationMillis
            );
        } finally {
            projectionLock.unlock();
        }
    }

    private void applyAndPublish(StoredEventRecord storedEventRecord) {
        ProjectionUpdateResponse update = projectionApplier.apply(PROJECTION_NAME, storedEventRecord);
        eventNotifier.notify(storedEventRecord.event());
        projectionSsePublisher.publish(update);
        log.debug("Applied SES projection update for request {} at stream version {}",
                update.workRequest().requestId(),
                update.workRequest().streamVersion());
    }
}
