package my.javacraft.echo.single.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
@RequiredArgsConstructor
public class SingleMessageListener implements Runnable {

    private final SingleNetworkManager singleNetworkManager;

    @Override
    public void run() {
        while(true) {
            Selector selector = singleNetworkManager.getSelector();
            SingleMessageSender singleMessageSender = singleNetworkManager.getSingleMessageSender();

            try {
                while(true) {
                    selector.select();
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while(keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        // Get the socket channel held by the key
                        SocketChannel channel = (SocketChannel)key.channel();

                        // Attempt a connection
                        if (key.isConnectable()) { // connect command
                            // Close pendent connections
                            if (channel.isConnectionPending()) {
                                channel.finishConnect();
                            }
                            singleMessageSender.setKey(key); // message will send after it
                        } else if(key.isReadable()) {
                            String message = newResponse(channel);
                            singleNetworkManager.addMessage(message);
                            System.out.println(message);
                        }
                    }
                }
            } catch (IOException | NegativeArraySizeException err) {
                singleNetworkManager.closeSocket();
                singleMessageSender.setKey(null);
            }
        }
    }

    public String newResponse(SocketChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

        try {
            int numRead = channel.read(buffer); // get message from client

            if(numRead == -1) {
                log.debug("Connection closed by: {}", channel.getRemoteAddress());
                channel.close();
            }

            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);

            // protobuf example
            // Protocol.Response response = Protocol.Response.parseFrom(data);
            // return response;
            return new String(data, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("Unable to read from channel", e);
            try {
                channel.close();
            } catch (IOException e1) {
                //nothing to do, channel dead
            }
        }
        return null;
    }
}
