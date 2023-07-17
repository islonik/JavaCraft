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

    @Autowired
    private EventsSubscriptionsManager eventsSubscriptionsManager;

    @Override
    public <T extends Event> void subscribe(Class<T> event, EventListener<T> listener) {
        eventsSubscriptionsManager.addSubscriber(event, listener);
    }

}