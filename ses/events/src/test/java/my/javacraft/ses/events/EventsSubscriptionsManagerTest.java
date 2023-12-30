package my.javacraft.ses.events;

import java.util.HashMap;
import java.util.Map;
import my.javacraft.ses.events.impl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventsSubscriptionsManagerTest {

    @Test
    public void testGetListeners() {
        Map<String, Event> storage = new HashMap<>();
        EventsSubscriptionsManager manager = createEventsSubscriptionsManager(storage);

        Assertions.assertEquals(1, manager.getListeners(AcceptedEvent.class).size());
        Assertions.assertEquals(1, manager.getListeners(CompletedEvent.class).size());
        Assertions.assertTrue(storage.isEmpty());
    }

    public static EventsSubscriptionsManager createEventsSubscriptionsManager(Map<String, Event> storage) {
        EventsSubscriptionsManager manager = new EventsSubscriptionsManager();
        Assertions.assertEquals(0, manager.getListeners(AcceptedEvent.class).size());

        EventListener<Event> eventListener = e -> storage.put(e.getTitle(), e);
        manager.addSubscriber(AcceptedEvent.class, eventListener);
        manager.addSubscriber(CompletedEvent.class, eventListener);
        manager.addSubscriber(CreatedEvent.class, eventListener);
        manager.addSubscriber(RejectedEvent.class, eventListener);
        manager.addSubscriber(RunningEvent.class, eventListener);

        return manager;
    }
}
