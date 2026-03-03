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

    /**
     * Feeds raw byte chunks directly so the test can split a UTF-8 code point
     * across reads without converting the bytes into broken Strings first.
     */
    private static final class ScriptedByteSocketChannel extends SocketChannel {
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
        protected void implCloseSelectableChannel() {
            // no-op
        }

        @Override
        protected void implConfigureBlocking(boolean block) {
            // no-op
        }
    }
}
