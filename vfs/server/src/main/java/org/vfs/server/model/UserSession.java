package org.vfs.server.model;

import lombok.Getter;
import lombok.Setter;
import org.vfs.core.network.protocol.Protocol.User;
import org.vfs.server.network.ClientWriter;

/**
 * @author Lipatov Nikita
 */
public class UserSession {
    @Setter
    @Getter
    private volatile User user;
    private final Timer timer;
    private final ClientWriter clientWriter;

    @Getter
    private volatile Node node;

    public UserSession(User user, ClientWriter clientWriter) {
        this.user = user;
        this.timer = new Timer();
        this.clientWriter = clientWriter;
    }

    public final Timer getTimer() {
        return timer;
    }

    public final ClientWriter getClientWriter() {
        return clientWriter;
    }

    public void setNode(Node node) {
        this.node = node;
        if (this.node.getType() != NodeTypes.DIR) {
            throw new IllegalArgumentException("UserSession: Node is not DIR!");
        }
    }

}
