package my.javacraft.ses.simulator.flow;

import my.javacraft.ses.events.Event;
import my.javacraft.ses.events.EventsMonitor;

import java.util.List;

/**
 * Created by nikilipa on 7/26/16.
 */
public class Reporter implements Runnable {

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(3000);

                List<Event> eventList = EventsMonitor.getStorage();
                if (!eventList.isEmpty()) {
                    System.out.println("*********************************************");
                    for (Event event : eventList) {
                        System.out.println(String.format(
                                "    Event priority = %s, title = %s, code = %s, estimate = %s, status = %s;",
                                event.getPriority(),
                                event.getTitle(),
                                event.getFinanceCode(),
                                event.getEstimate(),
                                event.getStatus()
                        ));
                    }
                    System.out.println("*********************************************");
                } else {
                    System.out.println("No events!");
                }
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
