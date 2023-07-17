package my.javacraft.ses.events;

import java.util.function.Consumer;

/**
 * Created by nikilipa on 7/23/16.
 */
@FunctionalInterface
public interface EventListener<T extends Event> extends Consumer<T> {
}

