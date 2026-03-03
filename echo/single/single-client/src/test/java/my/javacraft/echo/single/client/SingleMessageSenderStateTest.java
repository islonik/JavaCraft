package my.javacraft.echo.single.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the sender state without using real sockets.
 * <p>
 * The sender should only queue outbound data in send().
 * The actual write must happen later when the selector reports OP_WRITE.
 */
class SingleMessageSenderStateTest {

    @Test
    void testSendQueuesMessageAndEnablesWriteInterestWithoutWritingImmediately() {
        SingleMessageSender sender = new SingleMessageSender();
        TrackingSelector selector = new TrackingSelector();
        ScriptedWriteSocketChannel channel = new ScriptedWriteSocketChannel();
        TrackingSelectionKey key = new TrackingSelectionKey(channel);
        sender.setKey(key, selector);

        Assertions.assertDoesNotThrow(() -> sender.send("hello"));

        Assertions.assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.interestOps());
        Assertions.assertEquals("", channel.writtenText());
        Assertions.assertTrue(selector.wakeupCount > 0);
    }

    @Test
    void testFlushPendingWritesStopsOnZeroWriteAndKeepsWriteInterest() throws IOException {
        SingleMessageSender sender = new SingleMessageSender();
        TrackingSelector selector = new TrackingSelector();
        ScriptedWriteSocketChannel channel = new ScriptedWriteSocketChannel(3, 0, 4);
        TrackingSelectionKey key = new TrackingSelectionKey(channel);
        sender.setKey(key, selector);

        sender.send("hello");
        sender.flushPendingWrites();

        Assertions.assertEquals("hel", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.interestOps());
    }

    @Test
    void testFlushPendingWritesRestoresReadInterestAfterQueueDrains() throws IOException {
        SingleMessageSender sender = new SingleMessageSender();
        TrackingSelector selector = new TrackingSelector();
        ScriptedWriteSocketChannel channel = new ScriptedWriteSocketChannel(3, 0, 4);
        TrackingSelectionKey key = new TrackingSelectionKey(channel);
        sender.setKey(key, selector);

        sender.send("hello");
        sender.flushPendingWrites();
        sender.flushPendingWrites();

        Assertions.assertEquals("hello\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    /**
     * Records wakeups so the test can verify that send() notifies the selector
     * after it adds new outbound work to the queue.
     */
    private static final class TrackingSelector extends AbstractSelector {
        private int wakeupCount;

        private TrackingSelector() {
            super(SelectorProvider.provider());
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
            return Collections.emptySet();
        }

        @Override
        public int selectNow() {
            return 0;
        }

        @Override
        public int select(long timeout) {
            return 0;
        }

        @Override
        public int select() {
            return 0;
        }

        @Override
        public Selector wakeup() {
            wakeupCount++;
            return this;
        }
    }

    /**
     * Stores interest ops in memory so the tests can check when the sender
     * keeps or clears OP_WRITE.
     */
    private static final class TrackingSelectionKey extends AbstractSelectionKey {
        private final SocketChannel channel;
        private int interestOps = SelectionKey.OP_READ;

        private TrackingSelectionKey(SocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public SelectableChannel channel() {
            return channel;
        }

        @Override
        public Selector selector() {
            return null;
        }

        @Override
        @SuppressWarnings("MagicConstant")
        public int interestOps() {
            ensureValid();
            return interestOps;
        }

        @Override
        public SelectionKey interestOps(int ops) {
            ensureValid();
            interestOps = ops;
            return this;
        }

        @Override
        @SuppressWarnings("MagicConstant")
        public int readyOps() {
            return interestOps;
        }

        private void ensureValid() {
            if (!isValid()) {
                throw new CancelledKeyException();
            }
        }
    }

    /**
     * Replays a scripted sequence of non-blocking write results so the tests
     * can simulate partial progress and write stalls.
     */
    private static final class ScriptedWriteSocketChannel extends SocketChannel {
        private final ByteBuffer writes = ByteBuffer.allocate(4096);
        private final int[] writeResults;
        private int writeIndex;

        private ScriptedWriteSocketChannel(int... writeResults) {
            super(SelectorProvider.provider());
            this.writeResults = writeResults.clone();
        }

        String writtenText() {
            ByteBuffer copy = writes.duplicate();
            copy.flip();
            byte[] bytes = new byte[copy.remaining()];
            copy.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }

        @Override
        public int write(ByteBuffer src) {
            if (writeIndex < writeResults.length) {
                int scriptedResult = writeResults[writeIndex++];
                if (scriptedResult == 0) {
                    return 0;
                }
                return copyBytes(src, scriptedResult);
            }
            return copyBytes(src, src.remaining());
        }

        private int copyBytes(ByteBuffer src, int requestedBytes) {
            int bytesToCopy = Math.min(requestedBytes, src.remaining());
            byte[] data = new byte[bytesToCopy];
            src.get(data);
            writes.put(data);
            return bytesToCopy;
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
        public int read(ByteBuffer dst) {
            return 0;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) {
            return 0;
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
        public java.net.Socket socket() {
            return new java.net.Socket();
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
