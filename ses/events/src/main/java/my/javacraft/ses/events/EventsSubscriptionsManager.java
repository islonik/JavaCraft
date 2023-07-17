package my.javacraft.ses.events;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nikilipa on 7/25/16.
 */
@Service
public class EventsSubscriptionsManager {

    private final ConcurrentHashMap<Class, Collection> listeners = new ConcurrentHashMap<>();

    public <T extends Event> void addSubscriber(Class event, EventListener listener) {
        getListeners(event).add(listener);
    }

    public <T extends Event> Collection<EventListener<T>> getListeners(Class event) {
        Collection<EventListener<T>> newListeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Collection<EventListener<T>> oldListeners = listeners.putIfAbsent(event, newListeners);

        return oldListeners == null ? newListeners : oldListeners;
    }
}
