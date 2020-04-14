package com.github.derrop.proxy.api.connection;

import com.github.derrop.proxy.api.chat.component.BaseComponent;
import com.github.derrop.proxy.api.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

/**
 * A proxy connection is defined as a connection directly connected to a socket.
 * It should expose information about the remote peer, however not be specific
 * to a type of connection, whether server or player.
 */
public interface Connection extends PacketSender {

    /**
     * Gets the remote address of this connection.
     *
     * @return the remote address
     */
    SocketAddress getSocketAddress();

    /**
     * Disconnects this end of the connection for the specified reason. If this
     * is an {@link Player} the respective server connection will be
     * closed too.
     *
     * @param reason the reason shown to the player / sent to the server on
     *               disconnect
     */
    void disconnect(String reason);

    /**
     * Disconnects this end of the connection for the specified reason. If this
     * is an {@link Player} the respective server connection will be
     * closed too.
     *
     * @param reason the reason shown to the player / sent to the server on
     *               disconnect
     */
    void disconnect(BaseComponent... reason);

    /**
     * Disconnects this end of the connection for the specified reason. If this
     * is an {@link Player} the respective server connection will be
     * closed too.
     *
     * @param reason the reason shown to the player / sent to the server on
     *               disconnect
     */
    void disconnect(BaseComponent reason);

    /**
     * Gets whether this connection is currently open, ie: not disconnected, and
     * able to send / receive data.
     *
     * @return current connection status
     */
    boolean isConnected();

    void handleDisconnected(@NotNull ServiceConnection connection, @NotNull BaseComponent[] reason);
}
