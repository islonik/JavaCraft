# Simple Event System (ses)

<h2>Events-module</h2>
We have events package. This package provides functionality for event-system.
Conceptually we have next entities:
<ol>
<li>Event interface and classes which implements this interface. An event could happen by different reasons so it has different classes.</li>
<li>EventListener which listen to an event.</li>
<li>EventsMonitor declares how we will handle an event. (for example, we can store them in a database)</li>
<li>EventNotifier provides API for sending different type of events.</li> 
</ol>

<h2>Simulator-module</h2>
Application class creates WorkerLauncher component (using Spring DI) and launches it.
WorkerLauncher component creates 4 threads.
The first thread creates tasks.
The second thread validates tasks.
The third thread does a job for a task.
The last thread shows status for all tasks. It is basically a simple scheduler.