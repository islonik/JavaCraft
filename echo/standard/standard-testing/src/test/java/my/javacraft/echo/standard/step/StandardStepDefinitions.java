package my.javacraft.echo.standard.step;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import my.javacraft.echo.standard.client.async.StandardAsyncClient;
import my.javacraft.echo.standard.client.sync.StandardSyncClient;
import my.javacraft.echo.standard.server.MultithreadedServer;
import my.javacraft.echo.standard.server.ServerThread;
import org.junit.jupiter.api.Assertions;

public class StandardStepDefinitions {

    // ---------------------------------------------------------------------------
    // Access to ServerThread's shared static connection counter
    // ---------------------------------------------------------------------------

    /**
     * Direct reference to {@link ServerThread threads}.  Because that field is
     * {@code static final}, all ServerThread instances in this JVM (across every
     * test scenario) share the same counter.  We keep a reference here so that
     * {@link #awaitSharedThreadCounterReset()} can poll it efficiently without
     * repeated reflection in the hot loop.
     */
    private static final AtomicInteger SERVER_THREAD_COUNTER = resolveServerThreadCounter();

    private static AtomicInteger resolveServerThreadCounter() {
        try {
            Field field = ServerThread.class.getDeclaredField("threads");
            field.setAccessible(true);
            return (AtomicInteger) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // ---------------------------------------------------------------------------
    // Scenario state
    // ---------------------------------------------------------------------------

    private final Map<String, StandardSyncClient> syncConnections = new ConcurrentHashMap<>();
    private final Map<String, StandardAsyncClient> asyncConnections = new ConcurrentHashMap<>();
    private final List<ExecutorService> serverExecutors = new ArrayList<>();

    @After
    public void cleanup() {
        syncConnections.values().forEach(StandardSyncClient::close);
        syncConnections.clear();
        asyncConnections.values().forEach(StandardAsyncClient::close);
        asyncConnections.clear();
        serverExecutors.forEach(ExecutorService::shutdownNow);
        serverExecutors.clear();
        awaitSharedThreadCounterReset();
    }

    /**
     * Polls {@link ServerThread threads} until it reaches zero, or times out
     * after five seconds.
     *
     * <p>The counter is a JVM-wide static field shared by every scenario.
     * Closing a client socket causes the corresponding {@link ServerThread}
     * to receive {@code null} from {@code readLine()} and then decrement the
     * counter — but this decrement races with the next scenario's
     * {@code incrementAndGet()} in the {@link ServerThread} constructor.  If the
     * decrement loses that race, the "stats" command returns an inflated count
     * and the assertion fails.  Waiting here ensures all previous-scenario
     * threads have finished before the next scenario's server and clients start.
     */
    private void awaitSharedThreadCounterReset() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (SERVER_THREAD_COUNTER.get() > 0 && System.nanoTime() < deadline) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
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

    @When("sync client {string} connects on port {int}")
    public void connectSyncClient(String client, int port) {
        StandardSyncClient c = awaitSyncClientConnected(port);
        StandardSyncClient previous = syncConnections.putIfAbsent(client, c);
        if (previous != null) {
            c.close();
            Assertions.fail("Client '%s' already exists in this scenario".formatted(client));
        }
    }

    @When("async client {string} connects on port {int}")
    public void connectAsyncClient(String client, int port) {
        StandardAsyncClient c = awaitAsyncClientConnected(port);
        StandardAsyncClient previous = asyncConnections.putIfAbsent(client, c);
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

    @When("{int} sync clients with prefix {string} connect on port {int}")
    public void connectSyncClientsWithPrefix(int clientCount, String prefix, int port) {
        for (int i = 1; i <= clientCount; i++) {
            connectSyncClient(clientName(prefix, i), port);
        }
    }

    @When("{int} async clients with prefix {string} connect on port {int}")
    public void connectAsyncClientsWithPrefix(int clientCount, String prefix, int port) {
        for (int i = 1; i <= clientCount; i++) {
            connectAsyncClient(clientName(prefix, i), port);
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
     * Dispatches send/read/isClosed operations to whichever map (sync or async)
     * contains the named client, then asserts the response.
     */
    @SuppressWarnings("resource") // client is owned by asyncConnections and closed in cleanup()
    private void assertResponse(String clientName, String message, String expectedResponse) {
        StandardSyncClient sync = syncConnections.get(clientName);
        if (sync != null) {
            doAssertResponse(clientName, message, expectedResponse,
                    sync::sendMessage, sync::readMessage, sync::isClosedByServer);
            return;
        }
        StandardAsyncClient async = requireAsyncClient(clientName);
        doAssertResponse(clientName, message, expectedResponse,
                async::sendMessage, async::readMessage, async::isSocketClosed);
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
    @SuppressWarnings("resource") // client is owned by asyncConnections and closed in cleanup()
    private void performGoodbye(String clientName) {
        StandardSyncClient sync = syncConnections.get(clientName);
        if (sync != null) {
            doGoodbye(clientName, sync::sendMessage, sync::readMessage, sync::isClosedByServer);
            return;
        }
        StandardAsyncClient async = requireAsyncClient(clientName);
        doGoodbye(clientName, async::sendMessage, async::readMessage, async::isSocketClosed);
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
        StandardSyncClient sync = syncConnections.get(clientName);
        if (sync != null) {
            awaitSocketClosed(clientName, sync::isClosedByServer);
            return;
        }
        StandardAsyncClient async = requireAsyncClient(clientName);
        awaitSocketClosed(clientName, async::isSocketClosed);
    }

    /**
     * Retries client construction until the server is ready or fails after five seconds.
     */
    private StandardSyncClient awaitSyncClientConnected(int port) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (true) {
            try {
                StandardSyncClient client = new StandardSyncClient("sync-client", "localhost", port);
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

    private StandardAsyncClient awaitAsyncClientConnected(int port) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (true) {
            StandardAsyncClient client = new StandardAsyncClient(
                    "async-client-",
                    "localhost", port);
            if (client.isConnected()) {
                return client;
            }
            client.close();
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

    private StandardAsyncClient requireAsyncClient(String clientName) {
        StandardAsyncClient client = asyncConnections.get(clientName);
        Assertions.assertNotNull(client, "Client '%s' was not created in this scenario".formatted(clientName));
        return client;
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
                if (i < messagesPerClient) {
                    long delay = ThreadLocalRandom.current().nextLong(minDelayMs, maxDelayMs + 1L);
                    Thread.sleep(delay);
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
