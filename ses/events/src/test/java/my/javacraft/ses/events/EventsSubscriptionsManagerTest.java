package my.javacraft.ses.events;

import java.util.HashMap;
import java.util.Map;
import my.javacraft.ses.events.impl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventsSubscriptionsManagerTest {

    @Test
    public void testGetListeners() {
        EventsSubscriptionsManager manager = new EventsSubscriptionsManager();
        Assertions.assertEquals(0, manager.getListeners(AcceptedEvent.class).size());

        Map<String, Event> storage = new HashMap<>();

        EventListener<Event> eventListener = e -> storage.put(e.getTitle(), e);

        manager.addSubscriber(AcceptedEvent.class, eventListener);
        manager.addSubscriber(CompletedEvent.class, eventListener);
        manager.addSubscriber(CreatedEvent.class, eventListener);
        manager.addSubscriber(RejectedEvent.class, eventListener);
        manager.addSubscriber(RunningEvent.class, eventListener);

        Assertions.assertEquals(1, manager.getListeners(AcceptedEvent.class).size());
        Assertions.assertEquals(1, manager.getListeners(CompletedEvent.class).size());
        Assertions.assertTrue(storage.isEmpty());
    }
}
