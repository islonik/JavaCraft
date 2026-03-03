package my.javacraft.echo.single.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Covers the listener run-loop branches that are hard to reach with normal
 * socket tests, such as selector startup failure and error handling.
 */
class SingleMessageListenerRunTest {

    @Test
    void testRunStopsWhenSelectorIsUnavailable() {
        RecordingMessageSender sender = new RecordingMessageSender();
        RecordingNetworkManager manager = new RecordingNetworkManager(null, sender);
        SingleMessageListener listener = new SingleMessageListener(manager);

        listener.run();

        Assertions.assertFalse(manager.closeSocketCalled);
        Assertions.assertEquals(0, sender.clearCalls);
    }

    @Test
    void testRunClosesSocketAndClearsSenderOnIoException() {
        RecordingMessageSender sender = new RecordingMessageSender();
        sender.ioFailure = new IOException("flush failed");
        RecordingSelector selector = new RecordingSelector(new WritableReadyKey());
        RecordingNetworkManager manager = new RecordingNetworkManager(selector, sender);
        SingleMessageListener listener = new SingleMessageListener(manager);

        listener.run();

        Assertions.assertTrue(manager.closeSocketCalled);
        Assertions.assertEquals(1, sender.clearCalls);
    }

    @Test
    void testRunClosesSocketAndClearsSenderOnUnexpectedException() {
        RecordingMessageSender sender = new RecordingMessageSender();
        sender.runtimeFailure = new IllegalStateException("boom");
        RecordingSelector selector = new RecordingSelector(new WritableReadyKey());
        RecordingNetworkManager manager = new RecordingNetworkManager(selector, sender);
        SingleMessageListener listener = new SingleMessageListener(manager);

        listener.run();

        Assertions.assertTrue(manager.closeSocketCalled);
        Assertions.assertEquals(1, sender.clearCalls);
    }

    /**
     * Lets the tests inject a selector and observe whether the listener
     * requests socket cleanup from the network manager.
     */
    private static final class RecordingNetworkManager extends SingleNetworkManager {
        private final Selector selector;
        private final SingleMessageSender sender;
        private boolean closeSocketCalled;

        private RecordingNetworkManager(Selector selector, SingleMessageSender sender) {
            this.selector = selector;
            this.sender = sender;
        }

        @Override
        public Selector getSelector() {
            return selector;
        }

        @Override
        public SingleMessageSender getSingleMessageSender() {
            return sender;
        }

        @Override
        public void closeSocket() {
            closeSocketCalled = true;
        }
    }

    /**
     * Records whether the listener clears the sender state after the run loop
     * terminates because of an error.
     */
    private static final class RecordingMessageSender extends SingleMessageSender {
        private IOException ioFailure;
        private RuntimeException runtimeFailure;
        private int clearCalls;

        @Override
        void flushPendingWrites() throws IOException {
            if (ioFailure != null) {
                throw ioFailure;
            }
            if (runtimeFailure != null) {
                throw runtimeFailure;
            }
        }

        @Override
        public void setKey(SelectionKey key, Selector selector) {
            if (key == null && selector == null) {
                clearCalls++;
            }
        }
    }

    /**
     * Provides a single writable key so the listener reaches the sender flush
     * path immediately.
     */
    private static final class RecordingSelector extends AbstractSelector {
        private final Set<SelectionKey> selectedKeys = new LinkedHashSet<>();

        private RecordingSelector(SelectionKey key) {
            super(SelectorProvider.provider());
            selectedKeys.add(key);
        }

        @Override
        protected void implCloseSelector() {
            // no-op
        }

        @Override
        protected SelectionKey register(AbstractSelectableChannel ch, int ops, Object att) {
            throw new UnsupportedOperationException("register is not used in this test");
        }

        @Override
        public Set<SelectionKey> keys() {
            return Collections.emptySet();
        }

        @Override
        public Set<SelectionKey> selectedKeys() {
            return selectedKeys;
        }

        @Override
        public int selectNow() {
            return selectedKeys.size();
        }

        @Override
        public int select(long timeout) {
            return selectedKeys.size();
        }

        @Override
        public int select() {
            return selectedKeys.size();
        }

        @Override
        public Selector wakeup() {
            return this;
        }
    }

    /**
     * Exposes only writable readiness so the listener enters the flush path
     * without needing a real network channel.
     */
    private static final class WritableReadyKey extends SelectionKey {
        private final SocketChannel channel = new NoOpSocketChannel();

        @Override
        public SelectableChannel channel() {
            return channel;
        }

        @Override
        public Selector selector() {
            return null;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void cancel() {
            // no-op
        }

        @Override
        public int interestOps() {
            return SelectionKey.OP_WRITE;
        }

        @Override
        public SelectionKey interestOps(int ops) {
            return this;
        }

        @Override
        public int readyOps() {
            return SelectionKey.OP_WRITE;
        }
    }

    /**
     * Gives the writable ready key a channel instance without opening a real
     * connection because the run-loop error paths do not need actual I/O.
     */
    private static final class NoOpSocketChannel extends SocketChannel {
        private NoOpSocketChannel() {
            super(SelectorProvider.provider());
        }

        @Override
        public int read(ByteBuffer dst) {
            return 0;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) {
            return 0;
        }

        @Override
        public int write(ByteBuffer src) {
            int written = src.remaining();
            src.position(src.limit());
            return written;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) {
            long total = 0;
            for (int i = offset; i < offset + length; i++) {
                total += write(srcs[i]);
            }
            return total;
        }

        @Override
        public SocketChannel bind(SocketAddress local) {
            return this;
        }

        @Override
        public <T> SocketChannel setOption(SocketOption<T> name, T value) {
            return this;
        }

        @Override
        public SocketChannel shutdownInput() {
            return this;
        }

        @Override
        public SocketChannel shutdownOutput() {
            return this;
        }

        @Override
        public Socket socket() {
            return new Socket();
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public boolean isConnectionPending() {
            return false;
        }

        @Override
        public boolean connect(SocketAddress remote) {
            return true;
        }

        @Override
        public boolean finishConnect() {
            return true;
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return InetSocketAddress.createUnresolved("localhost", 8077);
        }

        @Override
        public SocketAddress getLocalAddress() {
            return InetSocketAddress.createUnresolved("localhost", 0);
        }

        @Override
        public <T> T getOption(SocketOption<T> name) {
            return null;
        }

        @Override
        public Set<SocketOption<?>> supportedOptions() {
            return Collections.emptySet();
        }

        @Override
        protected void implCloseSelectableChannel() {
            // no-op
        }

        @Override
        protected void implConfigureBlocking(boolean block) {
            // no-op
        }
    }
}
