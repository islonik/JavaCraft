# Socket Server (Standard)

Socket server using <b>blocking I/O</b> that demonstrates a simple client/server application in Java.

Every connection/client uses its own thread model implementation:
- `VirtualThreadClient` (virtual threads)
- `PlatformThreadClient` (platform daemon threads)

By definition, a <u>socket</u> is one endpoint of a two-way communication link between two programs running on different computers on a network. A socket is bound to a port number so that the transport layer can identify the application that data is destined to be sent to.

With blocking I/O, when a client makes a request to connect with the server, the thread that handles that connection is blocked until there is some data to read, or the data is fully written. Until the relevant operation is complete, that thread can do nothing else but wait.

## Modules

- `standard-client`: platform/virtual clients and client-side behavior tests.
- `standard-server`: multithreaded blocking socket server.
- `standard-testing`: Cucumber scenarios (functional + load + performance).

## Cucumber Performance Benchmark

Performance scenarios are implemented in `standard-testing` and are designed to run in separate fresh JVM processes:

1. Virtual only:
```bash
mvn -pl echo/standard/standard-testing -am test -Dcucumber.filter.tags='@Performance and @Virtual'
```

2. Platform only:
```bash
mvn -pl echo/standard/standard-testing -am test -Dcucumber.filter.tags='@Performance and @Platform'
```

3. Final comparison (reads persisted medians from `target/performance-results`):
```bash
mvn -pl echo/standard/standard-testing -am test -Dcucumber.filter.tags='@PerformanceSummary'
```

The benchmark flow includes warmups (not measured), 3 measured runs, and prints median-based comparison.

### Example Final Result

```text
[PERF][FINAL] virtual median: 2.054 s, throughput=48693.44 msg/s, avg=0.0205 ms/msg
[PERF][FINAL] platform median: 2.427 s, throughput=41205.25 msg/s, avg=0.0243 ms/msg
[PERF][FINAL] faster median: VIRTUAL by 15.38% (delta=0.373 s)
```

### Why Virtual Can Be Faster Here

- Virtual threads are much cheaper to park/unpark when many clients block on socket reads/writes.
- The JVM scheduler can multiplex many virtual threads over fewer carrier threads, reducing OS thread scheduling overhead.
- Platform threads incur higher per-thread OS cost (stack memory + kernel context switching), which becomes more visible under high concurrency.
- This benchmark is I/O-heavy with many blocking points, which is a workload where virtual threads often have an advantage.

### Important Notes

- Results are environment-specific (CPU, OS scheduler, JVM version, background load).
- Always compare medians from the same machine and same configuration.
- Use warmup iterations to reduce JIT/cold-start noise before measured runs.
