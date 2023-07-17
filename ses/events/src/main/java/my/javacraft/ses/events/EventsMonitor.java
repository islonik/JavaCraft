package my.javacraft.ses.events;

import my.javacraft.ses.events.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nikilipa on 7/25/16.
 */
@Service
public class EventsMonitor {

    private static final ConcurrentHashMap<String, Event> storage = new ConcurrentHashMap<>();

    @Autowired
    private EventsManager eventsManager;

    @PostConstruct
    public void post() {
        eventsManager.subscribe(AcceptedEvent.class, (e) -> {
            updateElement(e);
        });

        eventsManager.subscribe(CompletedEvent.class, (e) -> {
            updateElement(e);
        });

        eventsManager.subscribe(CreatedEvent.class, (e) -> {
            updateElement(e);
        });

        eventsManager.subscribe(RejectedEvent.class, (e) -> {
            updateElement(e);
        });

        eventsManager.subscribe(RunningEvent.class, (e) -> {
            updateElement(e);
        });
    }

    protected static void updateElement(Event e) {
        storage.put(e.getTitle(), e);
    }

    public static List<Event> getStorage() {
        List<Event> values = new ArrayList<>(storage.values());
        Collections.sort(values);
        return values;
    }

}
