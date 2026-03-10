package my.javacraft.echo.standard.step;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import my.javacraft.echo.standard.client.platform.PlatformThreadClient;
import my.javacraft.echo.standard.client.virtual.VirtualThreadClient;
import my.javacraft.echo.standard.server.MultithreadedServer;
import org.junit.jupiter.api.Assertions;

public class StandardStepDefinitions {

    private static final Object SYSTEM_OUT_LOCK = new Object();
    private static final Path PERFORMANCE_RESULTS_DIR = Path.of("target", "performance-results");

    // ---------------------------------------------------------------------------
    // Scenario state
    // ---------------------------------------------------------------------------

    private final Map<String, VirtualThreadClient> virtualConnections = new ConcurrentHashMap<>();
    private final Map<String, PlatformThreadClient> platformConnections = new ConcurrentHashMap<>();
    private final List<ExecutorService> serverExecutors = new ArrayList<>();

    @After
    public void cleanup() {
        virtualConnections.values().forEach(VirtualThreadClient::close);
        virtualConnections.clear();
        platformConnections.values().forEach(PlatformThreadClient::close);
        platformConnections.clear();
        serverExecutors.forEach(ExecutorService::shutdownNow);
        serverExecutors.clear();
    }

    // ---------------------------------------------------------------------------
    // Server step
    // ---------------------------------------------------------------------------

    @Given("the multithreaded server is running on port {int}")
    public void startMultithreadedServer(int port) {
        MultithreadedServer server = new MultithreadedServer(port);

        // Daemon threads allow the JVM to exit cleanly after tests complete,
        // preventing server sockets from lingering and blocking ports in subsequent runs.
        ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        executorService.execute(server);
        serverExecutors.add(executorService);
    }

    // ---------------------------------------------------------------------------
    // Client connect steps
    // ---------------------------------------------------------------------------

    @When("virtual client {string} connects on port {int}")
    public void connectVirtualClient(String client, int port) {
        VirtualThreadClient c = awaitVirtualClientConnected(port);
        VirtualThreadClient previous = virtualConnections.putIfAbsent(client, c);
        if (previous != null) {
            c.close();
            Assertions.fail("Client '%s' already exists in this scenario".formatted(client));
        }
    }

    @When("platform client {string} connects on port {int}")
    public void connectPlatformClient(String client, int port) {
        PlatformThreadClient c = awaitPlatformClientConnected(port);
        PlatformThreadClient previous = platformConnections.putIfAbsent(client, c);
        if (previous != null) {
            c.close();
            Assertions.fail("Client '%s' already exists in this scenario".formatted(client));
        }
    }

    // ---------------------------------------------------------------------------
    // Message steps
    // ---------------------------------------------------------------------------

    @Then("client {string} sends {string} and receives {string}")
    public void sendMessage(String client, String message, String expectedResponse) {
        assertResponse(client, message, expectedResponse);
    }

    @Then("client {string} sends escaped message {string} and receives escaped response {string}")
    public void sendEscapedMessage(String client, String message, String expectedResponse) {
        assertResponse(client, decodeEscapedText(message), decodeEscapedText(expectedResponse));
    }

    @Then("client {string} disconnects with goodbye")
    public void disconnectClientWithGoodbye(String client) {
        performGoodbye(client);
    }

    @Then("client {string} socket is closed")
    public void clientSocketIsClosed(String client) {
        resolveSocketClosed(client);
    }

    // ---------------------------------------------------------------------------
    // Bulk connect / disconnect steps for high-load scenarios
    // ---------------------------------------------------------------------------

    @When("{int} virtual clients with prefix {string} connect on port {int}")
    public void connectVirtualClientsWithPrefix(int clientCount, String prefix, int port) {
        for (int i = 1; i <= clientCount; i++) {
            connectVirtualClient(clientName(prefix, i), port);
        }
    }

    @When("{int} platform clients with prefix {string} connect on port {int}")
    public void connectPlatformClientsWithPrefix(int clientCount, String prefix, int port) {
        for (int i = 1; i <= clientCount; i++) {
            connectPlatformClient(clientName(prefix, i), port);
        }
    }

    @When("{int} clients with prefix {string} each send {int} echo messages from their own thread with a random delay between {int} and {int} milliseconds")
    public void clientsEachSendEchoMessagesWithRandomDelay(
            int clientCount,
            String prefix,
            int messagesPerClient,
            int minDelayMs,
            int maxDelayMs) {

        List<Thread> clientThreads = new ArrayList<>(clientCount);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (int i = 1; i <= clientCount; i++) {
            String currentClientName = clientName(prefix, i);
            Thread clientThread = Thread.ofPlatform()
                    .name("load-" + currentClientName)
                    .unstarted(() -> sendMessagesWithRandomDelay(
                            currentClientName,
                            messagesPerClient,
                            minDelayMs,
                            maxDelayMs,
                            failures));
            clientThreads.add(clientThread);
        }

        clientThreads.forEach(Thread::start);
        waitForClientThreads(clientThreads, failures);
        failIfAnyClientThreadFailed(failures);
    }

    @When("{word} thread performance benchmark runs {int} warmups and {int} measured runs with {int} clients and {int} messages on port {int}")
    public void runThreadPerformanceBenchmark(
            String threadType,
            int warmups,
            int measuredRuns,
            int clientCount,
            int messagesPerClient,
            int port) {

        Assertions.assertTrue(warmups >= 0, "Warmups must be zero or greater");
        Assertions.assertTrue(measuredRuns > 0, "Measured runs must be greater than zero");
        Assertions.assertTrue(clientCount > 0, "Client count must be greater than zero");
        Assertions.assertTrue(messagesPerClient > 0, "Messages per client must be greater than zero");

        String normalizedThreadType = threadType.toLowerCase();
        Assertions.assertTrue(
                "virtual".equals(normalizedThreadType) || "platform".equals(normalizedThreadType),
                "Unsupported thread type: " + threadType
        );

        PrintStream benchmarkOut = System.out;
        long benchmarkStartedAt = System.nanoTime();
        int totalMessages = clientCount * messagesPerClient;
        List<Long> measuredRunNanos = new ArrayList<>(measuredRuns);

        for (int warmup = 1; warmup <= warmups; warmup++) {
            String prefix = "%sPerfWarmup%02d".formatted(capitalize(normalizedThreadType), warmup);
            long elapsedNanos = runSingleBenchmarkIteration(
                    normalizedThreadType,
                    clientCount,
                    messagesPerClient,
                    port,
                    prefix
            );
            printRunMetrics(benchmarkOut, normalizedThreadType, "WARMUP", warmup, warmups,
                    clientCount, messagesPerClient, totalMessages, elapsedNanos);
        }

        for (int run = 1; run <= measuredRuns; run++) {
            String prefix = "%sPerfRun%02d".formatted(capitalize(normalizedThreadType), run);
            long elapsedNanos = runSingleBenchmarkIteration(
                    normalizedThreadType,
                    clientCount,
                    messagesPerClient,
                    port,
                    prefix
            );
            measuredRunNanos.add(elapsedNanos);
            printRunMetrics(benchmarkOut, normalizedThreadType, null, run, measuredRuns,
                    clientCount, messagesPerClient, totalMessages, elapsedNanos);
        }

        long benchmarkElapsedNanos = System.nanoTime() - benchmarkStartedAt;
        long measuredTotalNanos = measuredRunNanos.stream().mapToLong(Long::longValue).sum();
        double avgNanos = measuredTotalNanos / (double) measuredRuns;
        long medianNanos = medianNanos(measuredRunNanos);

        printAggregateMetrics(benchmarkOut, normalizedThreadType, measuredRuns,
                clientCount, messagesPerClient, totalMessages, avgNanos, medianNanos);

        writePerformanceSummary(
                normalizedThreadType,
                warmups,
                measuredRuns,
                clientCount,
                messagesPerClient,
                totalMessages,
                measuredTotalNanos,
                medianNanos,
                avgNanos,
                benchmarkElapsedNanos
        );
    }

    @Then("performance medians from separate JVM runs are compared and total execution time is printed")
    public void comparePerformanceMediansFromSeparateRuns() {
        PerformanceSummary virtual = readPerformanceSummary("virtual");
        PerformanceSummary platform = readPerformanceSummary("platform");

        Assertions.assertEquals(virtual.clientCount(), platform.clientCount(),
                "Virtual and platform client counts differ");
        Assertions.assertEquals(virtual.messagesPerClient(), platform.messagesPerClient(),
                "Virtual and platform messages-per-client differ");

        PrintStream out = System.out;
        out.printf(
                "[PERF][FINAL] virtual median: %.3f s, throughput=%.2f msg/s, avg=%.4f ms/msg%n",
                nanosToSeconds(virtual.medianNanos()),
                throughput(virtual.totalMessages(), virtual.medianNanos()),
                avgLatencyMillis(virtual.totalMessages(), virtual.medianNanos())
        );
        out.printf(
                "[PERF][FINAL] platform median: %.3f s, throughput=%.2f msg/s, avg=%.4f ms/msg%n",
                nanosToSeconds(platform.medianNanos()),
                throughput(platform.totalMessages(), platform.medianNanos()),
                avgLatencyMillis(platform.totalMessages(), platform.medianNanos())
        );

        String fasterType = virtual.medianNanos() <= platform.medianNanos() ? "VIRTUAL" : "PLATFORM";
        long fasterNanos = Math.min(virtual.medianNanos(), platform.medianNanos());
        long slowerNanos = Math.max(virtual.medianNanos(), platform.medianNanos());
        double improvementPercent = ((slowerNanos - fasterNanos) * 100.0) / slowerNanos;

        long combinedMeasuredNanos = virtual.measuredTotalNanos() + platform.measuredTotalNanos();
        long combinedScenarioNanos = virtual.scenarioElapsedNanos() + platform.scenarioElapsedNanos();

        out.printf(
                "[PERF][FINAL] faster median: %s by %.2f%% (delta=%.3f s)%n",
                fasterType,
                improvementPercent,
                nanosToSeconds(slowerNanos - fasterNanos)
        );
        out.printf(
                "[PERF][FINAL] total measured execution time: %.3f s (both tests)%n",
                nanosToSeconds(combinedMeasuredNanos)
        );
        out.printf(
                "[PERF][FINAL] total benchmark scenario time (warmups + measured): %.3f s (both tests)%n",
                nanosToSeconds(combinedScenarioNanos)
        );
    }

    @Then("{int} clients with prefix {string} disconnect with goodbye")
    public void clientsWithPrefixDisconnectWithGoodbye(int clientCount, String prefix) {
        for (int i = 1; i <= clientCount; i++) {
            performGoodbye(clientName(prefix, i));
        }
    }

    // ---------------------------------------------------------------------------
    // Private helpers — client resolution
    // ---------------------------------------------------------------------------

    /**
     * Dispatches send/read/isClosed operations to whichever map (virtual or platform)
     * contains the named client, then asserts the response.
     */
    @SuppressWarnings("resource") // client is owned by platformConnections and closed in cleanup()
    private void assertResponse(String clientName, String message, String expectedResponse) {
        VirtualThreadClient virtualClient = virtualConnections.get(clientName);
        if (virtualClient != null) {
            doAssertResponse(clientName, message, expectedResponse,
                    virtualClient::sendMessage, virtualClient::readMessage, virtualClient::isClosedByServer);
            return;
        }
        PlatformThreadClient platformClient = requirePlatformClient(clientName);
        doAssertResponse(clientName, message, expectedResponse,
                platformClient::sendMessage, platformClient::readMessage, () -> !platformClient.isConnected());
    }

    private void doAssertResponse(String clientName, String message, String expectedResponse,
            Consumer<String> send, Supplier<String> read, BooleanSupplier isClosed) {
        send.accept(message);
        String actual = read.get();
        Assertions.assertEquals(expectedResponse, actual,
                "Client '%s' sent '%s' but got unexpected response".formatted(clientName, message));
        if ("bye".equalsIgnoreCase(message)) {
            awaitSocketClosed(clientName, isClosed);
        }
    }

    /**
     * Sends "bye", asserts the farewell, waits for the server to close the
     * connection (which happens after the counter decrement), then returns.
     * The client socket itself is left open for {@code @After cleanup()} to close.
     */
    @SuppressWarnings("resource") // client is owned by platformConnections and closed in cleanup()
    private void performGoodbye(String clientName) {
        VirtualThreadClient virtualClient = virtualConnections.get(clientName);
        if (virtualClient != null) {
            doGoodbye(clientName, virtualClient::sendMessage, virtualClient::readMessage, virtualClient::isClosedByServer);
            return;
        }
        PlatformThreadClient platformClient = requirePlatformClient(clientName);
        doGoodbye(clientName, platformClient::sendMessage, platformClient::readMessage, () -> !platformClient.isConnected());
    }

    private void doGoodbye(String clientName, Consumer<String> send, Supplier<String> read,
            BooleanSupplier isClosed) {
        send.accept("bye");
        String actual = read.get();
        Assertions.assertEquals("Have a good day!", actual,
                "Client '%s' did not receive expected goodbye response".formatted(clientName));
        awaitSocketClosed(clientName, isClosed);
    }

    private void resolveSocketClosed(String clientName) {
        VirtualThreadClient virtualClient = virtualConnections.get(clientName);
        if (virtualClient != null) {
            awaitSocketClosed(clientName, virtualClient::isClosedByServer);
            return;
        }
        PlatformThreadClient platformClient = requirePlatformClient(clientName);
        awaitSocketClosed(clientName, () -> !platformClient.isConnected());
    }

    /**
     * Retries client construction until the server is ready or fails after five seconds.
     */
    private VirtualThreadClient awaitVirtualClientConnected(int port) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (true) {
            try {
                VirtualThreadClient client = new VirtualThreadClient("virtual-client", "localhost", port);
                if (client.isConnected()) {
                    return client;
                }
                client.close();
            } catch (IllegalStateException ignored) {
                // fail-fast constructor can throw while server is still starting; keep retrying until deadline.
            }
            if (System.nanoTime() >= deadline) {
                Assertions.fail("Server not ready on port " + port + " within 5 seconds");
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }

    private PlatformThreadClient awaitPlatformClientConnected(int port) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (true) {
            try {
                PlatformThreadClient client = new PlatformThreadClient("platform-client", "localhost", port);
                if (client.isConnected()) {
                    return client;
                }
                client.close();
            } catch (IllegalStateException ignored) {
                // fail-fast constructor can throw while server is still starting; keep retrying until deadline.
            }
            if (System.nanoTime() >= deadline) {
                Assertions.fail("Server not ready on port " + port + " within 5 seconds");
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }

    /**
     * Blocks until the client's listener thread detects the server-side EOF, or
     * fails after two seconds.  After the server processes "bye" it closes its
     * streams (sending a TCP FIN), and the listener thread detects EOF and sets
     * the {@code socketClosed} flag.  This flag is set only <em>after</em> the
     * server's counter decrement, so waiting here guarantees the shared counter
     * is up to date before the caller proceeds.
     */
    private void awaitSocketClosed(String clientName, BooleanSupplier isClosed) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (isClosed.getAsBoolean()) {
                return;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
        Assertions.assertTrue(isClosed.getAsBoolean(),
                "Client '%s' socket should be closed after goodbye".formatted(clientName));
    }

    private PlatformThreadClient requirePlatformClient(String clientName) {
        PlatformThreadClient client = platformConnections.get(clientName);
        Assertions.assertNotNull(client, "Client '%s' was not created in this scenario".formatted(clientName));
        return client;
    }

    private long runSingleBenchmarkIteration(
            String normalizedThreadType,
            int clientCount,
            int messagesPerClient,
            int port,
            String prefix) {
        return withSuppressedSystemOut(() -> {
            connectBenchmarkClients(normalizedThreadType, clientCount, prefix, port);

            assertResponse(
                    clientName(prefix, 1),
                    "stats",
                    "Simultaneously connected clients: %d".formatted(clientCount)
            );

            long startedAt = System.nanoTime();
            clientsEachSendEchoMessagesWithRandomDelay(clientCount, prefix, messagesPerClient, 0, 0);
            long elapsedNanos = System.nanoTime() - startedAt;

            clientsWithPrefixDisconnectWithGoodbye(clientCount, prefix);
            return elapsedNanos;
        });
    }

    private void connectBenchmarkClients(String normalizedThreadType, int clientCount, String prefix, int port) {
        for (int i = 1; i <= clientCount; i++) {
            String currentClientName = clientName(prefix, i);
            if ("virtual".equals(normalizedThreadType)) {
                connectVirtualClient(currentClientName, port);
            } else {
                connectPlatformClient(currentClientName, port);
            }
        }
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private long medianNanos(List<Long> measuredRunNanos) {
        List<Long> sorted = measuredRunNanos.stream().sorted(Comparator.naturalOrder()).toList();
        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        }
        long left = sorted.get((size / 2) - 1);
        long right = sorted.get(size / 2);
        return (left + right) / 2;
    }

    private void printRunMetrics(
            PrintStream out,
            String threadType,
            String runType,
            int run,
            int totalRuns,
            int clientCount,
            int messagesPerClient,
            int totalMessages,
            long elapsedNanos) {
        String runTypeSuffix = runType == null ? "" : "[" + runType + "]";
        out.printf(
                "[PERF][%s]%s run %d/%d: clients=%d, messages/client=%d, total=%d, elapsed=%.3f s, throughput=%.2f msg/s, avg=%.4f ms/msg%n",
                threadType.toUpperCase(),
                runTypeSuffix,
                run,
                totalRuns,
                clientCount,
                messagesPerClient,
                totalMessages,
                nanosToSeconds(elapsedNanos),
                throughput(totalMessages, elapsedNanos),
                avgLatencyMillis(totalMessages, elapsedNanos)
        );
    }

    private void printAggregateMetrics(
            PrintStream out,
            String threadType,
            int measuredRuns,
            int clientCount,
            int messagesPerClient,
            int totalMessages,
            double avgNanos,
            long medianNanos) {
        out.printf(
                "[PERF][%s] average over %d measured runs: clients=%d, messages/client=%d, total=%d, elapsed=%.3f s, throughput=%.2f msg/s, avg=%.4f ms/msg%n",
                threadType.toUpperCase(),
                measuredRuns,
                clientCount,
                messagesPerClient,
                totalMessages,
                nanosToSeconds(avgNanos),
                throughput(totalMessages, avgNanos),
                avgLatencyMillis(totalMessages, avgNanos)
        );
        out.printf(
                "[PERF][%s] median over %d measured runs: clients=%d, messages/client=%d, total=%d, elapsed=%.3f s, throughput=%.2f msg/s, avg=%.4f ms/msg%n",
                threadType.toUpperCase(),
                measuredRuns,
                clientCount,
                messagesPerClient,
                totalMessages,
                nanosToSeconds(medianNanos),
                throughput(totalMessages, medianNanos),
                avgLatencyMillis(totalMessages, medianNanos)
        );
    }

    private void writePerformanceSummary(
            String threadType,
            int warmups,
            int measuredRuns,
            int clientCount,
            int messagesPerClient,
            int totalMessages,
            long measuredTotalNanos,
            long medianNanos,
            double averageNanos,
            long scenarioElapsedNanos) {
        Properties properties = new Properties();
        properties.setProperty("threadType", threadType);
        properties.setProperty("warmups", Integer.toString(warmups));
        properties.setProperty("measuredRuns", Integer.toString(measuredRuns));
        properties.setProperty("clientCount", Integer.toString(clientCount));
        properties.setProperty("messagesPerClient", Integer.toString(messagesPerClient));
        properties.setProperty("totalMessages", Integer.toString(totalMessages));
        properties.setProperty("measuredTotalNanos", Long.toString(measuredTotalNanos));
        properties.setProperty("medianNanos", Long.toString(medianNanos));
        properties.setProperty("averageNanos", Double.toString(averageNanos));
        properties.setProperty("scenarioElapsedNanos", Long.toString(scenarioElapsedNanos));

        try {
            Files.createDirectories(PERFORMANCE_RESULTS_DIR);
            Path summaryFile = performanceSummaryFile(threadType);
            try (var writer = Files.newBufferedWriter(summaryFile, StandardCharsets.UTF_8)) {
                properties.store(writer, "Performance benchmark summary for " + threadType);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist benchmark results for " + threadType, e);
        }
    }

    private PerformanceSummary readPerformanceSummary(String threadType) {
        Path summaryFile = performanceSummaryFile(threadType);
        Assertions.assertTrue(Files.exists(summaryFile),
                "No persisted benchmark summary for '%s'. Run '@Performance and @%s' first."
                        .formatted(threadType, capitalize(threadType)));

        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(summaryFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read benchmark summary for " + threadType, e);
        }

        return new PerformanceSummary(
                Integer.parseInt(properties.getProperty("clientCount")),
                Integer.parseInt(properties.getProperty("messagesPerClient")),
                Integer.parseInt(properties.getProperty("totalMessages")),
                Long.parseLong(properties.getProperty("measuredTotalNanos")),
                Long.parseLong(properties.getProperty("medianNanos")),
                Long.parseLong(properties.getProperty("scenarioElapsedNanos"))
        );
    }

    private Path performanceSummaryFile(String threadType) {
        return PERFORMANCE_RESULTS_DIR.resolve(threadType + ".properties");
    }

    private double nanosToSeconds(long nanos) {
        return nanos / 1_000_000_000.0;
    }

    private double nanosToSeconds(double nanos) {
        return nanos / 1_000_000_000.0;
    }

    private double throughput(int totalMessages, long elapsedNanos) {
        return throughput(totalMessages, (double) elapsedNanos);
    }

    private double throughput(int totalMessages, double elapsedNanos) {
        return totalMessages / (elapsedNanos / 1_000_000_000.0);
    }

    private double avgLatencyMillis(int totalMessages, long elapsedNanos) {
        return avgLatencyMillis(totalMessages, (double) elapsedNanos);
    }

    private double avgLatencyMillis(int totalMessages, double elapsedNanos) {
        return (elapsedNanos / 1_000_000.0) / totalMessages;
    }

    private <T> T withSuppressedSystemOut(Supplier<T> supplier) {
        synchronized (SYSTEM_OUT_LOCK) {
            PrintStream originalOut = System.out;
            try (PrintStream suppressedOut =
                         new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8)) {
                System.setOut(suppressedOut);
                return supplier.get();
            } finally {
                System.setOut(originalOut);
            }
        }
    }

    private record PerformanceSummary(
            int clientCount,
            int messagesPerClient,
            int totalMessages,
            long measuredTotalNanos,
            long medianNanos,
            long scenarioElapsedNanos) {
    }

    /**
     * Runs the load-work loop for one client so the high-load scenario keeps
     * every client on its own platform thread.
     */
    private void sendMessagesWithRandomDelay(
            String clientName,
            int messagesPerClient,
            int minDelayMs,
            int maxDelayMs,
            ConcurrentLinkedQueue<Throwable> failures) {
        try {
            for (int i = 1; i <= messagesPerClient; i++) {
                String message = "%s message %03d".formatted(clientName, i);
                assertResponse(clientName, message, "Did you say '%s'?".formatted(message));
                if (i < messagesPerClient && maxDelayMs > 0) {
                    long delay = ThreadLocalRandom.current().nextLong(minDelayMs, maxDelayMs + 1L);
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }
            }
        } catch (Throwable t) {
            failures.add(t);
        }
    }

    private void waitForClientThreads(List<Thread> threads, ConcurrentLinkedQueue<Throwable> failures) {
        for (Thread thread : threads) {
            try {
                thread.join(TimeUnit.SECONDS.toMillis(30));
                if (thread.isAlive()) {
                    failures.add(new AssertionError(
                            "Load client thread '%s' did not finish in time".formatted(thread.getName())));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                failures.add(e);
                return;
            }
        }
    }

    private void failIfAnyClientThreadFailed(ConcurrentLinkedQueue<Throwable> failures) {
        if (failures.isEmpty()) {
            return;
        }
        AssertionError combined = new AssertionError("High-load client threads reported failures");
        failures.forEach(combined::addSuppressed);
        throw combined;
    }

    private String clientName(String prefix, int index) {
        return "%s-%03d".formatted(prefix, index);
    }

    private String decodeEscapedText(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current != '\\' || i + 1 >= text.length()) {
                result.append(current);
                continue;
            }
            char escaped = text.charAt(++i);
            switch (escaped) {
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case '\\' -> result.append('\\');
                case '\'' -> result.append('\'');
                case '"' -> result.append('"');
                default -> {
                    result.append('\\');
                    result.append(escaped);
                }
            }
        }
        return result.toString();
    }
}
