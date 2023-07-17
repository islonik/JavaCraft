package my.javacraft.ses.events.impl;

import my.javacraft.ses.events.EventStatus;
import my.javacraft.ses.events.Priority;

/**
 * Created by nikilipa on 7/26/16.
 */
public class RejectedEvent extends BaseEvent {

    public RejectedEvent(Priority priority, String title, String financeCode, int estimate) {
        super(priority, title, financeCode, estimate, EventStatus.REJECTED);
    }
}
