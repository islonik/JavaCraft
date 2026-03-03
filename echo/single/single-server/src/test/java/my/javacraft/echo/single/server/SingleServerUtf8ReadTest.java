package my.javacraft.echo.single.server;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the server keeps raw bytes buffered until a full UTF-8 frame
 * is available for decoding.
 */
class SingleServerUtf8ReadTest {

    @Test
    void testReadDecodesMultibyteCharacterSplitAcrossReads() {
        SingleServer server = new SingleServer(0);
        byte[] payload = "Привет\r\n".getBytes(StandardCharsets.UTF_8);
        ScriptedByteSocketChannel channel = new ScriptedByteSocketChannel(
                Arrays.copyOfRange(payload, 0, 1),
                Arrays.copyOfRange(payload, 1, payload.length));

        String result = server.read(channel);

        Assertions.assertEquals("Привет\r\n", result);
    }

    @Test
    void testReadClosesChannelWhenFrameExceedsMaxBytes() {
        SingleServer server = new SingleServer(0);
        byte[] oversizedFrame = new byte[SingleServer.MAX_FRAME_BYTES + 1];
        Arrays.fill(oversizedFrame, (byte) 'A');
        ScriptedByteSocketChannel channel = new ScriptedByteSocketChannel(
                splitIntoChunks(oversizedFrame));

        String result = server.read(channel);

        Assertions.assertEquals("", result);
        Assertions.assertFalse(channel.isOpen(), "Oversized frame should close the channel");
    }

    @Test
    void testReadIgnoresCloseFailureWhenOversizedFrameIsRejected() {
        SingleServer server = new SingleServer(0);
        byte[] oversizedFrame = new byte[SingleServer.MAX_FRAME_BYTES + 1];
        Arrays.fill(oversizedFrame, (byte) 'A');
        CloseFailingScriptedByteSocketChannel channel = new CloseFailingScriptedByteSocketChannel(
                splitIntoChunks(oversizedFrame));

        String result = server.read(channel);

        Assertions.assertEquals("", result);
        Assertions.assertEquals(1, channel.closeAttempts, "Oversized request should still attempt channel close");
    }

    @Test
    void testReadClosesChannelWhenDelimitedFrameExceedsMaxBytes() {
        SingleServer server = new SingleServer(0);
        byte[] oversizedFrame = new byte[SingleServer.MAX_FRAME_BYTES + 3];
        Arrays.fill(oversizedFrame, 0, SingleServer.MAX_FRAME_BYTES + 1, (byte) 'A');
        oversizedFrame[SingleServer.MAX_FRAME_BYTES + 1] = '\r';
        oversizedFrame[SingleServer.MAX_FRAME_BYTES + 2] = '\n';
        ScriptedByteSocketChannel channel = new ScriptedByteSocketChannel(
                splitIntoChunks(oversizedFrame));

        String result = server.read(channel);

        Assertions.assertEquals("", result);
        Assertions.assertFalse(channel.isOpen(), "Delimited oversized request should close the channel");
    }

    @Test
    void testOpenServerChannelCreatesSocketWhenPortIsZero() throws Exception {
        assumeSocketBindingAvailable();
        SingleServer server = new SingleServer(0);

        java.lang.reflect.Method openServerChannel = SingleServer.class.getDeclaredMethod("openServerChannel");
        openServerChannel.setAccessible(true);

        try (ServerSocketChannel channel = (ServerSocketChannel) openServerChannel.invoke(server)) {
            Assertions.assertTrue(channel.isOpen());
            Assertions.assertNotNull(channel.getLocalAddress());
        }
    }

    /**
     * Splits a large synthetic payload into read-sized chunks so the scripted
     * channel behaves like the production non-blocking socket reads.
     */
    private static byte[][] splitIntoChunks(byte[] payload) {
        int chunkSize = SingleServer.BUFFER_SIZE;
        int chunks = (payload.length + chunkSize - 1) / chunkSize;
        byte[][] result = new byte[chunks][];
        for (int index = 0; index < chunks; index++) {
            int start = index * chunkSize;
            int end = Math.min(payload.length, start + chunkSize);
            result[index] = Arrays.copyOfRange(payload, start, end);
        }
        return result;
    }

    /**
     * Skips the port-zero startup test when the environment forbids local
     * socket binding, which happens in this sandbox but not in normal runs.
     */
    private static void assumeSocketBindingAvailable() {
        try (ServerSocketChannel probe = ServerSocketChannel.open()) {
            probe.bind(new InetSocketAddress(0));
        } catch (Exception blocked) {
            Assumptions.assumeTrue(false, "Local socket binding is unavailable in this environment");
        }
    }

    /**
     * Lets the oversized-request test hit the defensive close-error branch
     * without relying on a real socket that fails during close().
     */
    private static final class CloseFailingScriptedByteSocketChannel extends ScriptedByteSocketChannel {
        private int closeAttempts;

        private CloseFailingScriptedByteSocketChannel(byte[]... chunks) {
            super(chunks);
        }

        @Override
        protected void implCloseSelectableChannel() throws java.io.IOException {
            closeAttempts++;
            throw new java.io.IOException("close failed");
        }
    }

    /**
     * Feeds raw byte chunks directly so the test can break UTF-8 code points
     * at arbitrary byte boundaries and still exercise the server read path.
     */
    private static class ScriptedByteSocketChannel extends SocketChannel {
        private final byte[][] chunks;
        private int chunkIndex;

        private ScriptedByteSocketChannel(byte[]... chunks) {
            super(SelectorProvider.provider());
            this.chunks = chunks.clone();
        }

        @Override
        public int read(ByteBuffer dst) {
            if (chunkIndex >= chunks.length) {
                return 0;
            }

            byte[] chunk = chunks[chunkIndex++];
            dst.put(chunk);
            return chunk.length;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) {
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
        protected void implCloseSelectableChannel() throws java.io.IOException {
            // no-op
        }

        @Override
        protected void implConfigureBlocking(boolean block) {
            // no-op
        }
    }
}
