package my.javacraft.echo.single.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
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
 * Verifies request normalization and queued write behavior in isolation by
 * calling the private read/write selector handlers through reflection.
 */
class SingleServerNormalizationTest {

    @Test
    void testWriteOpPreservesEmbeddedLineBreaks() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel("hello\nworld\r\n");
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        invokeReadOp(server, key);
        Assertions.assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.interestOps());

        invokeWriteOp(server, key);

        Assertions.assertEquals("Did you say 'hello\nworld'?\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testWriteOpKeepsPendingWriteInterestWhenSocketStopsAcceptingBytes() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel("hello\r\n", 5, 0, 1024);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        invokeReadOp(server, key);
        invokeWriteOp(server, key);

        Assertions.assertTrue(key.isValid(), "Partial flush must keep the key alive for the next writable event");
        Assertions.assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.interestOps());
        Assertions.assertEquals("Did y", channel.writtenText());

        invokeWriteOp(server, key);

        Assertions.assertEquals("Did you say 'hello'?\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testWriteOpClosesAfterQueuedGoodbyeIsFullyDrained() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel("bye\r\n", 4, 0, 1024);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);
        setConnectionsToOne(server);

        invokeReadOp(server, key);
        invokeWriteOp(server, key);

        Assertions.assertTrue(key.isValid(), "Goodbye should wait until queued bytes are flushed");
        Assertions.assertEquals(1, getConnections(server));
        Assertions.assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.interestOps());

        invokeWriteOp(server, key);

        Assertions.assertEquals("Have a good day!\r\n", channel.writtenText());
        Assertions.assertFalse(key.isValid(), "Channel should close after the goodbye response is sent");
        Assertions.assertEquals(0, getConnections(server));
    }

    @Test
    void testWriteOpRespondsToStatsRequest() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel("stats\r\n");
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        invokeReadOp(server, key);
        invokeWriteOp(server, key);

        Assertions.assertEquals("Simultaneously connected clients: 0\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testWriteOpRespondsToEmptyRequest() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel("\r\n");
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        invokeReadOp(server, key);
        invokeWriteOp(server, key);

        Assertions.assertEquals("Please type something.\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testWriteOpReturnsToReadWhenNothingQueued() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel("");
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);

        invokeWriteOp(server, key);

        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
        Assertions.assertTrue(key.isValid());
    }

    @Test
    void testWriteOpClosesKeyWhenQueuedWriteFails() throws Exception {
        SingleServer server = new SingleServer(0);
        FailingWriteSocketChannel channel = new FailingWriteSocketChannel("hello\r\n");
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        invokeReadOp(server, key);
        invokeWriteOp(server, key);

        Assertions.assertFalse(key.isValid());
    }

    @Test
    void testWriteOpFlushesEachBufferedFrameFromSingleRead() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel("first\r\nsecond\r\n");
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        invokeReadOp(server, key);
        invokeWriteOp(server, key);

        Assertions.assertEquals(
                "Did you say 'first'?\r\nDid you say 'second'?\r\n",
                channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    /**
     * Keeps reflection details in one place so each test can focus on the
     * selector behavior it is asserting.
     */
    private static void invokeReadOp(SingleServer server, SelectionKey key) throws Exception {
        Method readOp = SingleServer.class.getDeclaredMethod("readOp", SelectionKey.class);
        readOp.setAccessible(true);
        readOp.invoke(server, key);
    }

    /**
     * Keeps reflection details in one place so each test can focus on the
     * queued write result instead of private method lookup.
     */
    private static void invokeWriteOp(SingleServer server, SelectionKey key) throws Exception {
        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);
    }

    /**
     * Lets the goodbye test assert that the server decrements its connection
     * counter only after the queued response is fully sent.
     */
    private static int getConnections(SingleServer server) throws Exception {
        var field = SingleServer.class.getDeclaredField("connections");
        field.setAccessible(true);
        return ((java.util.concurrent.atomic.AtomicInteger) field.get(server)).get();
    }

    /**
     * Avoids exposing the production counter directly while still letting the
     * goodbye test exercise the close-after-write branch.
     */
    private static void setConnectionsToOne(SingleServer server) throws Exception {
        var field = SingleServer.class.getDeclaredField("connections");
        field.setAccessible(true);
        ((java.util.concurrent.atomic.AtomicInteger) field.get(server)).set(1);
    }

    private static final class FakeSelectionKey extends SelectionKey {
        private final SocketChannel channel;
        private int interestOps;
        private boolean valid = true;

        private FakeSelectionKey(SocketChannel channel, int interestOps) {
            this.channel = channel;
            this.interestOps = interestOps;
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
            return interestOps;
        }
    }

    /**
     * Simulates one readable request plus configurable non-blocking write
     * progress so tests can exercise partial flushes deterministically.
     */
    private static class ScriptedSocketChannel extends SocketChannel {
        private final byte[] inboundBytes;
        private final int[] writePlan;
        private final ByteArrayOutputStream writtenBytes = new ByteArrayOutputStream();
        private boolean readConsumed;
        private int writeCalls;

        private ScriptedSocketChannel(String inboundText, int... writePlan) {
            super(SelectorProvider.provider());
            this.inboundBytes = inboundText.getBytes(StandardCharsets.UTF_8);
            this.writePlan = writePlan.clone();
        }

        String writtenText() {
            return writtenBytes.toString(StandardCharsets.UTF_8);
        }

        @Override
        public int read(ByteBuffer dst) {
            if (readConsumed || inboundBytes.length == 0) {
                return 0;
            }

            dst.put(inboundBytes);
            readConsumed = true;
            return inboundBytes.length;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) {
            long total = 0;
            for (int index = offset; index < offset + length; index++) {
                int read = read(dsts[index]);
                if (read <= 0) {
                    return total == 0 ? read : total;
                }
                total += read;
            }
            return total;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            int permittedBytes = src.remaining();
            if (writeCalls < writePlan.length) {
                permittedBytes = Math.min(permittedBytes, writePlan[writeCalls]);
            }
            writeCalls++;

            if (permittedBytes == 0) {
                return 0;
            }

            byte[] bytes = new byte[permittedBytes];
            src.get(bytes);
            writtenBytes.write(bytes);
            return permittedBytes;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
            long total = 0;
            for (int index = offset; index < offset + length; index++) {
                total += write(srcs[index]);
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

    /**
     * Forces the server down its queued-write failure path without needing a
     * real socket that throws during write().
     */
    private static final class FailingWriteSocketChannel extends ScriptedSocketChannel {
        private FailingWriteSocketChannel(String inboundText) {
            super(inboundText);
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            throw new IOException("write failed");
        }
    }
}
