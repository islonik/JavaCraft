package dev.nklip.javacraft.ses.app.controller;

import dev.nklip.javacraft.ses.api.query.RebuildProjectionsResponse;
import dev.nklip.javacraft.ses.app.service.ProjectionCoordinator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/projections")
public class AdminProjectionController {

    private final ProjectionCoordinator projectionCoordinator;

    public AdminProjectionController(ProjectionCoordinator projectionCoordinator) {
        this.projectionCoordinator = projectionCoordinator;
    }

    @PostMapping("/rebuild")
    public ResponseEntity<RebuildProjectionsResponse> rebuildProjections() {
        return ResponseEntity.ok(projectionCoordinator.rebuildProjections());
    }
}
