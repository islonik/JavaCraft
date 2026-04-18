package dev.nklip.javacraft.ses.app.controller;

import dev.nklip.javacraft.ses.api.query.BudgetProjectionResponse;
import dev.nklip.javacraft.ses.app.service.WorkRequestQueryService;
import dev.nklip.javacraft.ses.app.service.ProjectionSsePublisher;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/projections")
public class ProjectionController {

    private final WorkRequestQueryService workRequestQueryService;
    private final ProjectionSsePublisher projectionSsePublisher;

    public ProjectionController(
            WorkRequestQueryService workRequestQueryService,
            ProjectionSsePublisher projectionSsePublisher
    ) {
        this.workRequestQueryService = workRequestQueryService;
        this.projectionSsePublisher = projectionSsePublisher;
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetProjectionResponse>> getBudgets() {
        return ResponseEntity.ok(workRequestQueryService.getBudgets());
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProjectionUpdates() {
        return projectionSsePublisher.subscribe();
    }
}
