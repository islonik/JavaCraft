package my.javacraft.echo.single.server;

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
 * Verifies request normalization in isolation by calling the private writeOp()
 * method via reflection and capturing the response through lightweight fake SelectionKey/SocketChannel implementations.
 * This keeps the test focused on line-ending handling without depending on real sockets or the full server loop.
 */
class SingleServerNormalizationTest {

    @Test
    void testWriteOpPreservesEmbeddedLineBreaks() throws Exception {
        SingleServer server = new SingleServer(0);
        RecordingSocketChannel channel = new RecordingSocketChannel();
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach("hello\nworld\r\n");

        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertEquals("Did you say 'hello\nworld'?\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
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
        public int interestOps() {
            return interestOps;
        }

        @Override
        public SelectionKey interestOps(int ops) {
            interestOps = ops;
            return this;
        }

        @Override
        public int readyOps() {
            return interestOps;
        }
    }

    private static final class RecordingSocketChannel extends SocketChannel {
        private final ByteBuffer writes = ByteBuffer.allocate(4096);
        private boolean open = true;

        private RecordingSocketChannel() {
            super(SelectorProvider.provider());
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
            int len = src.remaining();
            writes.put(src);
            return len;
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
