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
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests how the listener handles ready keys in one select cycle.
 * <p>
 * The same key can be writable and readable at the same time, so the listener
 * must process both states instead of treating them as mutually exclusive.
 */
class SingleMessageListenerReadyKeyTest {

    @Test
    void testProcessReadyKeyFlushesWritesAndReadsResponsesInSameCycle() throws IOException {
        SingleNetworkManager manager = new SingleNetworkManager();
        RecordingMessageSender sender = new RecordingMessageSender();
        manager.setSingleMessageSender(sender);
        SingleMessageListener listener = new SingleMessageListener(manager);

        ReadyKey key = new ReadyKey(new ReadableSocketChannel("pong\r\n"), SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        listener.processReadyKey(key, sender);

        Assertions.assertTrue(sender.flushCalled);
        Assertions.assertEquals("pong", manager.getMessage());
    }

    @Test
    void testProcessReadyKeyReadsWithoutFlushingWhenOnlyReadable() throws IOException {
        SingleNetworkManager manager = new SingleNetworkManager();
        RecordingMessageSender sender = new RecordingMessageSender();
        manager.setSingleMessageSender(sender);
        SingleMessageListener listener = new SingleMessageListener(manager);

        ReadableSocketChannel channel = new ReadableSocketChannel("pong\r\n");
        ReadyKey key = new ReadyKey(channel, SelectionKey.OP_READ);

        listener.processReadyKey(key, sender);

        Assertions.assertFalse(sender.flushCalled);
        Assertions.assertEquals("pong", manager.getMessage());
        Assertions.assertTrue(channel.readCalls > 0, "Readable key should consume response bytes");
    }

    @Test
    void testProcessReadyKeyFlushesWithoutReadingWhenOnlyWritable() throws IOException {
        SingleNetworkManager manager = new SingleNetworkManager();
        RecordingMessageSender sender = new RecordingMessageSender();
        manager.setSingleMessageSender(sender);
        SingleMessageListener listener = new SingleMessageListener(manager);

        ReadableSocketChannel channel = new ReadableSocketChannel("pong\r\n");
        ReadyKey key = new ReadyKey(channel, SelectionKey.OP_WRITE);

        listener.processReadyKey(key, sender);

        Assertions.assertTrue(sender.flushCalled);
        Assertions.assertNull(manager.getMessage());
        Assertions.assertEquals(0, channel.readCalls, "Writable-only key should not read response bytes");
    }

    /**
     * Records whether the listener asks the sender to flush queued writes.
     */
    private static final class RecordingMessageSender extends SingleMessageSender {
        private boolean flushCalled;

        @Override
        void flushPendingWrites() {
            flushCalled = true;
        }
    }

    /**
     * Lets the test control which readiness flags are visible to the listener.
     */
    private static final class ReadyKey extends SelectionKey {
        private final SocketChannel channel;
        private final int readyOps;
        private int interestOps;
        private boolean valid = true;

        private ReadyKey(SocketChannel channel, int readyOps) {
            this.channel = channel;
            this.readyOps = readyOps;
            this.interestOps = readyOps;
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
        public boolean isValid() {
            return valid;
        }

        @Override
        public void cancel() {
            valid = false;
        }

        @Override
        @SuppressWarnings("MagicConstant")
        public int interestOps() {
            return interestOps;
        }

        @Override
        public SelectionKey interestOps(int ops) {
            interestOps = ops;
            return this;
        }

        @Override
        @SuppressWarnings("MagicConstant")
        public int readyOps() {
            return readyOps;
        }
    }

    /**
     * Provides one framed response so the test can verify the listener still
     * reads data after it handles OP_WRITE.
     */
    private static final class ReadableSocketChannel extends SocketChannel {
        private final byte[] payload;
        private int position;
        private int readCalls;
        private boolean open = true;

        private ReadableSocketChannel(String payload) {
            super(SelectorProvider.provider());
            this.payload = payload.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (!open) {
                throw new IOException("channel closed");
            }
            readCalls++;
            if (position >= payload.length) {
                return 0;
            }

            int bytesToCopy = Math.min(dst.remaining(), payload.length - position);
            dst.put(payload, position, bytesToCopy);
            position += bytesToCopy;
            return bytesToCopy;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
            long total = 0;
            for (int i = offset; i < offset + length; i++) {
                int read = read(dsts[i]);
                if (read <= 0) {
                    return total == 0 ? read : total;
                }
                total += read;
            }
            return total;
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
            open = false;
        }

        @Override
        protected void implConfigureBlocking(boolean block) {
            // no-op
        }
    }
}
