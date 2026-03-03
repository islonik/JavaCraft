package my.javacraft.echo.single.client;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the listener decodes UTF-8 only after a full framed message
 * has been collected from the byte stream.
 */
class SingleMessageListenerUtf8Test {

    @Test
    void testNewResponseDecodesMultibyteCharacterSplitAcrossReads() {
        byte[] payload = "Привет\r\n".getBytes(StandardCharsets.UTF_8);
        ScriptedByteSocketChannel channel = new ScriptedByteSocketChannel(
                Arrays.copyOfRange(payload, 0, 1),
                Arrays.copyOfRange(payload, 1, payload.length));
        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());

        String result = listener.newResponse(channel);

        Assertions.assertEquals("Привет", result);
    }

    @Test
    void testNewResponseClosesChannelWhenFrameExceedsMaxBytes() {
        byte[] oversizedFrame = new byte[SingleMessageListener.MAX_FRAME_BYTES + 1];
        Arrays.fill(oversizedFrame, (byte) 'A');
        ScriptedByteSocketChannel channel = new ScriptedByteSocketChannel(
                splitIntoChunks(oversizedFrame));
        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());

        String result = listener.newResponse(channel);

        Assertions.assertNull(result);
        Assertions.assertFalse(channel.isOpen(), "Oversized frame should close the channel");
    }

    @Test
    void testNewResponseIgnoresCloseFailureWhenOversizedFrameIsRejected() {
        byte[] oversizedFrame = new byte[SingleMessageListener.MAX_FRAME_BYTES + 1];
        Arrays.fill(oversizedFrame, (byte) 'A');
        CloseFailingScriptedByteSocketChannel channel = new CloseFailingScriptedByteSocketChannel(
                splitIntoChunks(oversizedFrame));
        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());

        String result = listener.newResponse(channel);

        Assertions.assertNull(result);
        Assertions.assertEquals(1, channel.closeAttempts, "Oversized frame should still attempt channel close");
    }

    @Test
    void testNewResponseClosesChannelWhenDelimitedFrameExceedsMaxBytes() {
        byte[] oversizedFrame = new byte[SingleMessageListener.MAX_FRAME_BYTES + 3];
        Arrays.fill(oversizedFrame, 0, SingleMessageListener.MAX_FRAME_BYTES + 1, (byte) 'A');
        oversizedFrame[SingleMessageListener.MAX_FRAME_BYTES + 1] = '\r';
        oversizedFrame[SingleMessageListener.MAX_FRAME_BYTES + 2] = '\n';
        ScriptedByteSocketChannel channel = new ScriptedByteSocketChannel(
                splitIntoChunks(oversizedFrame));
        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());

        String result = listener.newResponse(channel);

        Assertions.assertNull(result);
        Assertions.assertFalse(channel.isOpen(), "Delimited oversized frame should close the channel");
    }

    @Test
    void testNewResponseIgnoresCloseFailureWhenDelimitedFrameIsRejected() {
        byte[] oversizedFrame = new byte[SingleMessageListener.MAX_FRAME_BYTES + 3];
        Arrays.fill(oversizedFrame, 0, SingleMessageListener.MAX_FRAME_BYTES + 1, (byte) 'A');
        oversizedFrame[SingleMessageListener.MAX_FRAME_BYTES + 1] = '\r';
        oversizedFrame[SingleMessageListener.MAX_FRAME_BYTES + 2] = '\n';
        CloseFailingScriptedByteSocketChannel channel = new CloseFailingScriptedByteSocketChannel(
                splitIntoChunks(oversizedFrame));
        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());

        String result = listener.newResponse(channel);

        Assertions.assertNull(result);
        Assertions.assertEquals(1, channel.closeAttempts, "Delimited oversized frame should still attempt channel close");
    }

    /**
     * Splits a large synthetic payload into read-sized chunks so the scripted
     * channel behaves like the production non-blocking socket reads.
     */
    private static byte[][] splitIntoChunks(byte[] payload) {
        int chunkSize = SingleMessageListener.BUFFER_SIZE;
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
     * Lets the oversized-frame test hit the defensive close-error branch
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
            throw new java.nio.channels.ClosedChannelException();
        }
    }

    /**
     * Feeds raw byte chunks directly so the test can split a UTF-8 code point
     * across reads without converting the bytes into broken Strings first.
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
