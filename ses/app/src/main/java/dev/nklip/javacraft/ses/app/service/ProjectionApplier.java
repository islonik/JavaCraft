package dev.nklip.javacraft.ses.app.service;

import dev.nklip.javacraft.ses.api.query.BudgetProjectionResponse;
import dev.nklip.javacraft.ses.api.query.ProjectionUpdateResponse;
import dev.nklip.javacraft.ses.api.query.WorkRequestResponse;
import dev.nklip.javacraft.ses.app.repository.BudgetProjectionRepository;
import dev.nklip.javacraft.ses.app.repository.ProjectionCheckpointRepository;
import dev.nklip.javacraft.ses.app.repository.WorkRequestProjectionRepository;
import dev.nklip.javacraft.ses.app.model.StoredEventRecord;
import dev.nklip.javacraft.ses.events.Event;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectionApplier {

    private final WorkRequestProjectionRepository workRequestProjectionRepository;
    private final BudgetProjectionRepository budgetProjectionRepository;
    private final ProjectionCheckpointRepository projectionCheckpointRepository;

    public ProjectionApplier(
            WorkRequestProjectionRepository workRequestProjectionRepository,
            BudgetProjectionRepository budgetProjectionRepository,
            ProjectionCheckpointRepository projectionCheckpointRepository
    ) {
        this.workRequestProjectionRepository = workRequestProjectionRepository;
        this.budgetProjectionRepository = budgetProjectionRepository;
        this.projectionCheckpointRepository = projectionCheckpointRepository;
    }

    @Transactional
    public ProjectionUpdateResponse apply(String projectionName, StoredEventRecord storedEventRecord) {
        Event event = storedEventRecord.event();
        String requestedBy = workRequestProjectionRepository.findByRequestId(event.getTaskId())
                .map(WorkRequestResponse::requestedBy)
                .orElse(event.getActor());

        WorkRequestResponse workRequest = new WorkRequestResponse(
                event.getTaskId(),
                event.getTitle(),
                event.getPriority(),
                event.getBudgetCode(),
                event.getEstimate(),
                event.getStatus(),
                requestedBy,
                event.getActor(),
                event.getReason(),
                event.getEventId(),
                event.getOccurredAt(),
                event.getStreamVersion()
        );

        workRequestProjectionRepository.upsert(workRequest);
        budgetProjectionRepository.recalculate(event.getBudgetCode(), event.getOccurredAt());
        projectionCheckpointRepository.save(projectionName, storedEventRecord.storeId());

        BudgetProjectionResponse budget = budgetProjectionRepository.findByBudgetCode(event.getBudgetCode())
                .orElseThrow(() -> new IllegalStateException(
                        "Missing budget projection for code " + event.getBudgetCode()
                ));

        return new ProjectionUpdateResponse(
                "WORK_REQUEST_UPDATED",
                event.getEventId(),
                event.getOccurredAt(),
                workRequest,
                budget
        );
    }
}
