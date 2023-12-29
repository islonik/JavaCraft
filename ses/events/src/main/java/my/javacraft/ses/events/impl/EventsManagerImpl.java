package my.javacraft.ses.events.impl;

import my.javacraft.ses.events.Event;
import my.javacraft.ses.events.EventsSubscriptionsManager;
import my.javacraft.ses.events.EventListener;
import my.javacraft.ses.events.EventsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nikilipa on 7/25/16.
 */
@Service
public class EventsManagerImpl implements EventsManager {

    private final EventsSubscriptionsManager eventsSubscriptionsManager;

    @Autowired
    public EventsManagerImpl(EventsSubscriptionsManager eventsSubscriptionsManager) {
        this.eventsSubscriptionsManager = eventsSubscriptionsManager;
    }

    @Override
    public void subscribe(Class<? extends Event> event, EventListener<Event> listener) {
        eventsSubscriptionsManager.addSubscriber(event, listener);
    }

}