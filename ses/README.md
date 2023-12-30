# Simple Event System (ses)

Initially created in 2016.

## Events-module
We have <b>events</b> package. This package provides core functionality for event-system.

Conceptually we have next entities:
1. Event interface and classes which implements this interface. An event could happen by different reasons so it has different classes.
2. EventListener which listen to an event.
3. EventsMonitor declares how we will handle an event. (for example, we can store them in a database)
4. EventNotifier provides API for sending different type of events.

## Simulator-module
Application class creates WorkerLauncher component (using Spring DI) and launches it.

WorkerLauncher component creates 4 threads:
* The first thread creates tasks.
* The second thread validates tasks.
* The third thread does a job for a task.
* The last thread shows status for all tasks. It is basically a simple scheduler.