package my.javacraft.echo.single.client;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the startup lifecycle of {@link SingleClient}.
 * <p>
 * The important rule is simple: the listener thread must not start in the constructor.
 * It should start only after the client connects successfully.
 * <p>
 * These tests use small fake objects instead of real sockets so the behavior is easy to verify.
 */
class SingleClientLifecycleTest {

    @Test
    void testConstructorDoesNotStartListenerBeforeConnect() {
        RecordingNetworkManager networkManager = new RecordingNetworkManager();
        RecordingExecutorService executor = new RecordingExecutorService();

        new SingleClient("localhost", 8077, networkManager, executor);

        Assertions.assertEquals(0, executor.submittedTaskCount);
    }

    @Test
    void testConnectToServerStartsListenerAfterSuccessfulOpen() throws IOException {
        RecordingNetworkManager networkManager = new RecordingNetworkManager();
        RecordingExecutorService executor = new RecordingExecutorService();
        SingleClient client = new SingleClient("localhost", 8077, networkManager, executor);

        client.connectToServer();

        Assertions.assertEquals(1, networkManager.openAttempts);
        Assertions.assertEquals(1, executor.submittedTaskCount);
    }

    @Test
    void testConnectFailureDoesNotStartListenerAndSuccessfulRetryDoes() throws IOException {
        RecordingNetworkManager networkManager = new RecordingNetworkManager();
        networkManager.failNextOpen(new IOException("connection refused"));
        RecordingExecutorService executor = new RecordingExecutorService();
        SingleClient client = new SingleClient("localhost", 8077, networkManager, executor);

        Assertions.assertThrows(IOException.class, client::connectToServer);
        Assertions.assertEquals(0, executor.submittedTaskCount);

        client.connectToServer();

        Assertions.assertEquals(2, networkManager.openAttempts);
        Assertions.assertEquals(1, executor.submittedTaskCount);
    }

    private static final class RecordingNetworkManager extends SingleNetworkManager {
        private final Deque<IOException> failures = new ArrayDeque<>();
        private int openAttempts;

        private void failNextOpen(IOException failure) {
            failures.addLast(failure);
        }

        @Override
        public void openSocket(String serverHost, int serverPort) throws IOException {
            openAttempts++;
            IOException failure = failures.pollFirst();
            if (failure != null) {
                throw failure;
            }
        }
    }

    private static final class RecordingExecutorService extends AbstractExecutorService {
        private boolean shutdown;
        private int submittedTaskCount;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public @NonNull List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) {
            return shutdown;
        }

        @Override
        public void execute(@NonNull Runnable command) {
            submittedTaskCount++;
        }
    }
}
