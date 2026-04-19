package dev.nklip.javacraft.ewrs.app.service;

import dev.nklip.javacraft.ewrs.api.query.ProjectionUpdateResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Manages SSE subscribers and pushes projection updates to them.
 * Architecture mapping: implements the final {@code projector -> sse -> stream} hop from the Runtime Topology and
 * Projection Flow.
 */
@Service
public class ProjectionSsePublisher {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ignored -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    public void publish(ProjectionUpdateResponse update) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("projection-update")
                        .data(update));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
