package net.md_5.bungee.connection;

import de.derrop.minecraft.proxy.command.CommandSender;
import de.derrop.minecraft.proxy.connection.ConnectedProxyClient;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.score.Scoreboard;

import java.util.UUID;

/**
 * Represents a player who's connection is being connected to somewhere else,
 * whether it be a remote or embedded server.
 */
public interface ProxiedPlayer extends Connection, CommandSender {

    /**
     * Represents the player's chat state.
     */
    enum ChatMode {

        /**
         * The player will see all chat.
         */
        SHOWN,
        /**
         * The player will only see everything except messages marked as chat.
         */
        COMMANDS_ONLY,
        /**
         * The chat is completely disabled, the player won't see anything.
         */
        HIDDEN

    }

    enum MainHand {

        LEFT,
        RIGHT
    }

    /**
     * Gets this player's display name.
     *
     * @return the players current display name
     */
    String getDisplayName();

    /**
     * Sets this players display name to be used as their nametag and tab list
     * name.
     *
     * @param name the name to set
     */
    void setDisplayName(String name);

    /**
     * Send a message to the specified screen position of this player.
     *
     * @param position the screen position
     * @param message  the message to send
     */
    void sendMessage(ChatMessageType position, BaseComponent... message);

    /**
     * Send a message to the specified screen position of this player.
     *
     * @param position the screen position
     * @param message  the message to send
     */
    void sendMessage(ChatMessageType position, BaseComponent message);

    /**
     * Send a plugin message to this player.
     * <p>
     * In recent Minecraft versions channel names must contain a colon separator
     * and consist of [a-z0-9/._-]. This will be enforced in a future version.
     * The "BungeeCord" channel is an exception and may only take this form.
     *
     * @param channel the channel to send this data via
     * @param data    the data to send
     */
    void sendData(String channel, byte[] data);

    void useClient(ConnectedProxyClient proxyClient);

    ConnectedProxyClient getConnectedClient();

    void disableAutoReconnect();

    void enableAutoReconnect();

    String getName();

    /**
     * Get the pending connection that belongs to this player.
     *
     * @return the pending connection that this player used
     */
    PendingConnection getPendingConnection();

    /**
     * Make this player chat (say something), to the server he is currently on.
     *
     * @param message the message to say
     */
    void chat(String message);

    /**
     * Get this connection's UUID, if set.
     *
     * @return the UUID
     * @deprecated In favour of {@link #getUniqueId()}
     */
    @Deprecated
    String getUUID();

    /**
     * Get this connection's UUID, if set.
     *
     * @return the UUID
     */
    UUID getUniqueId();

    /**
     * Set the header and footer displayed in the tab player list.
     *
     * @param header The header for the tab player list, null to clear it.
     * @param footer The footer for the tab player list, null to clear it.
     */
    void setTabHeader(BaseComponent header, BaseComponent footer);

    /**
     * Set the header and footer displayed in the tab player list.
     *
     * @param header The header for the tab player list, null to clear it.
     * @param footer The footer for the tab player list, null to clear it.
     */
    void setTabHeader(BaseComponent[] header, BaseComponent[] footer);

    /**
     * Clears the header and footer displayed in the tab player list.
     */
    void resetTabHeader();

    /**
     * Sends a {@link Title} to this player. This is the same as calling
     * {@link Title#send(ProxiedPlayer)}.
     *
     * @param title The title to send to the player.
     * @see Title
     */
    void sendTitle(Title title);

    /**
     * Get the {@link Scoreboard} that belongs to this player.
     *
     * @return this player's {@link Scoreboard}
     */
    Scoreboard getScoreboard();
}
