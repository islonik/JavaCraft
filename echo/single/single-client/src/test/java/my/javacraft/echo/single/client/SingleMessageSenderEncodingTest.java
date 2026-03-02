package my.javacraft.echo.single.client;

import java.io.ByteArrayOutputStream;
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
 * Verifies that SingleMessageSender always writes UTF-8 bytes by launching a
 * separate JVM with a different default charset and running a small probe inside it.
 * <p>
 * The probe writes through a fake SocketChannel/SelectionKey pair and prints
 * the raw hex payload so the assertion checks encoded bytes, not decoded text.
 */
class SingleMessageSenderEncodingTest {

    @Test
    void testSendUsesUtf8EvenWhenDefaultCharsetDiffers() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
                javaExecutable(),
                "-Dfile.encoding=ISO-8859-1",
                "-cp",
                System.getProperty("java.class.path"),
                SingleMessageSenderEncodingProbe.class.getName());
        processBuilder.environment().remove("JAVA_TOOL_OPTIONS");

        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        String errorOutput = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        Assertions.assertEquals(0, exitCode, errorOutput);
        Assertions.assertEquals("D09FD180D0B8D0B2D0B5D1820D0A", output);
    }

    private static String javaExecutable() {
        return System.getProperty("java.home") + "/bin/java";
    }
}

final class SingleMessageSenderEncodingProbe {

    private SingleMessageSenderEncodingProbe() {
    }

    public static void main(String[] args) {
        RecordingSocketChannel channel = new RecordingSocketChannel();
        FakeSelectionKey key = new FakeSelectionKey(channel);
        SingleMessageSender sender = new SingleMessageSender();
        sender.setKey(key);
        sender.send("Привет");
        System.out.print(channel.hexDump());
    }

    private static final class FakeSelectionKey extends SelectionKey {
        private final SocketChannel channel;
        private int interestOps = SelectionKey.OP_READ;

        private FakeSelectionKey(SocketChannel channel) {
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
        public boolean isValid() {
            return true;
        }

        @Override
        public void cancel() {
            // no-op
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
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        private RecordingSocketChannel() {
            super(SelectorProvider.provider());
        }

        String hexDump() {
            byte[] data = bytes.toByteArray();
            StringBuilder result = new StringBuilder(data.length * 2);
            for (byte value : data) {
                result.append(String.format("%02X", value));
            }
            return result.toString();
        }

        @Override
        public int write(ByteBuffer src) {
            int len = src.remaining();
            byte[] data = new byte[len];
            src.get(data);
            bytes.writeBytes(data);
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
        protected void implCloseSelectableChannel() throws IOException {
            // no-op
        }

        @Override
        protected void implConfigureBlocking(boolean block) throws IOException {
            // no-op
        }
    }
}
