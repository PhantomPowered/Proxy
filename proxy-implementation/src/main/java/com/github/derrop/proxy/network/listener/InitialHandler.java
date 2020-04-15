package com.github.derrop.proxy.network.listener;

import com.github.derrop.proxy.Constants;
import com.github.derrop.proxy.MCProxy;
import com.github.derrop.proxy.api.chat.ChatColor;
import com.github.derrop.proxy.api.chat.component.BaseComponent;
import com.github.derrop.proxy.api.chat.component.TextComponent;
import com.github.derrop.proxy.api.connection.PendingConnection;
import com.github.derrop.proxy.api.connection.ProtocolState;
import com.github.derrop.proxy.api.connection.ServiceConnection;
import com.github.derrop.proxy.api.network.Packet;
import com.github.derrop.proxy.api.event.EventManager;
import com.github.derrop.proxy.api.events.connection.player.PlayerLoginEvent;
import com.github.derrop.proxy.api.network.PacketHandler;
import com.github.derrop.proxy.api.network.channel.NetworkChannel;
import com.github.derrop.proxy.api.util.Callback;
import com.github.derrop.proxy.connection.PlayerUniqueTabList;
import com.github.derrop.proxy.entity.player.DefaultPlayer;
import com.github.derrop.proxy.network.NetworkUtils;
import com.github.derrop.proxy.network.channel.ChannelListener;
import com.github.derrop.proxy.network.cipher.PacketCipherDecoder;
import com.github.derrop.proxy.network.cipher.PacketCipherEncoder;
import com.github.derrop.proxy.network.handler.HandlerEndpoint;
import com.github.derrop.proxy.protocol.ProtocolIds;
import com.github.derrop.proxy.protocol.handshake.PacketHandshakingInSetProtocol;
import com.github.derrop.proxy.protocol.legacy.PacketLegacyPing;
import com.github.derrop.proxy.protocol.login.PacketLoginEncryptionRequest;
import com.github.derrop.proxy.protocol.login.PacketLoginEncryptionResponse;
import com.github.derrop.proxy.protocol.login.PacketLoginLoginRequest;
import com.github.derrop.proxy.protocol.login.PacketPlayServerLoginSuccess;
import com.github.derrop.proxy.protocol.play.server.PacketPlayServerKickPlayer;
import com.github.derrop.proxy.protocol.play.shared.PacketPlayPluginMessage;
import com.github.derrop.proxy.protocol.status.PacketStatusPing;
import com.github.derrop.proxy.protocol.status.PacketStatusRequest;
import com.github.derrop.proxy.protocol.status.PacketStatusResponse;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.md_5.bungee.BufUtil;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.ServerPing;
import net.md_5.bungee.Util;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InitialHandler implements PendingConnection, ChannelListener {

    private final MCProxy proxy;

    private NetworkChannel networkChannel;
    @Getter
    private PacketHandshakingInSetProtocol packetHandshakingInSetProtocol;
    @Getter
    private PacketLoginLoginRequest loginRequest;
    private PacketLoginEncryptionRequest request;
    @Getter
    private final List<PacketPlayPluginMessage> relayMessages = new ArrayList<>();
    private State thisState = State.HANDSHAKE;
    @Getter
    private boolean onlineMode = true;
    @Getter
    private InetSocketAddress virtualHost;
    private String name;
    @Getter
    private UUID uniqueId;
    @Getter
    private UUID offlineId;
    @Getter
    private LoginResult loginProfile;
    @Getter
    private boolean legacy;
    @Getter
    private String extraDataInHandshake = "";

    public InitialHandler(MCProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void sendPacket(@NotNull Packet packet) {
        this.ch.write(packet);
    }

    @Override
    public void sendPacket(@NotNull ByteBuf byteBuf) {
        this.ch.write(byteBuf);
    }

    @Override
    public @NotNull NetworkUnsafe networkUnsafe() {
        return new NetworkUnsafe() {
            @Override
            public void sendPacket(@NotNull Object packet) {
                ch.write(packet);
            }
        };
    }

    private enum State {

        HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHED
    }

    private boolean canSendKickMessage() {
        return thisState == State.USERNAME || thisState == State.ENCRYPT || thisState == State.FINISHED;
    }

    @Override
    public void handleChannelActive(@NotNull NetworkChannel channel) {
        this.networkChannel = channel;
    }

    @Override
    public void handleException(@NotNull NetworkChannel channel, @NotNull Throwable cause) {
        if (this.canSendKickMessage()) {
            this.disconnect(ChatColor.RED + Util.exception(cause));
        } else {
            channel.close();
        }
    }

    @PacketHandler(packetIds = {ProtocolIds.ClientBound.Play.CUSTOM_PAYLOAD}, protocolState = ProtocolState.PLAY)
    public void handle(PacketPlayPluginMessage pluginMessage) throws Exception {
        if (PacketPlayPluginMessage.SHOULD_RELAY.apply(pluginMessage)) {
            relayMessages.add(pluginMessage);
        }
    }

    @Override
    public void handle(PacketLegacyPing ping) throws Exception {
        this.legacy = true;
        final boolean v1_5 = ping.isV1_5();

        ServerPing legacy = new ServerPing(new ServerPing.Protocol("§cProxy by §bderrop", -1),
                new ServerPing.Players(0, 0, null),
                new TextComponent(TextComponent.fromLegacyText("§7Please use the MC Version §c47")), null);

        String kickMessage;

        if (v1_5) {
            kickMessage = ChatColor.DARK_BLUE
                    + "\00" + 127
                    + '\00' + legacy.getVersion().getName()
                    + '\00' + getFirstLine(legacy.getDescription())
                    + '\00' + legacy.getPlayers().getOnline()
                    + '\00' + legacy.getPlayers().getMax();
        } else {
            // Clients <= 1.3 don't support colored motds because the color char is used as delimiter
            kickMessage = ChatColor.stripColor(getFirstLine(legacy.getDescription()))
                    + '\u00a7' + legacy.getPlayers().getOnline()
                    + '\u00a7' + legacy.getPlayers().getMax();
        }

        ch.close(kickMessage);
    }

    private static String getFirstLine(String str) {
        int pos = str.indexOf('\n');
        return pos == -1 ? str : str.substring(0, pos);
    }

    private ServerPing getPingInfo(String motd, int protocol) {
        return new ServerPing(
                new ServerPing.Protocol("§cProxy by §bderrop", -1),
                new ServerPing.Players(0, 0, null),
                new TextComponent(TextComponent.fromLegacyText(motd)),
                null
        );
    }

    @Override
    public void handle(PacketStatusRequest statusRequest) throws Exception {
        Preconditions.checkState(thisState == State.STATUS, "Not expecting STATUS");

        final String motd = "§7To join: Contact §6Schul_Futzi#4633 §7on §9Discord\n§7Available/Online Accounts: §e" + MCProxy.getInstance().getFreeClients().size() + "§7/§e" + MCProxy.getInstance().getOnlineClients().size();
        final int protocol = (ProtocolConstants.SUPPORTED_VERSION_IDS.contains(packetHandshakingInSetProtocol.getProtocolVersion())) ? packetHandshakingInSetProtocol.getProtocolVersion() : 578;

        this.ch.write(new PacketStatusResponse(Util.GSON.toJson(getPingInfo(motd, protocol))));

        thisState = State.PING;
    }

    @Override
    public void handle(PacketStatusPing ping) throws Exception {
        Preconditions.checkState(thisState == State.PING, "Not expecting PING");
        this.ch.write(ping);
        disconnect("");
    }

    @Override
    public void handle(PacketHandshakingInSetProtocol packetHandshakingInSetProtocol) throws Exception {
        Preconditions.checkState(thisState == State.HANDSHAKE, "Not expecting HANDSHAKE");
        this.packetHandshakingInSetProtocol = packetHandshakingInSetProtocol;
        ch.setVersion(packetHandshakingInSetProtocol.getProtocolVersion());

        // Starting with FML 1.8, a "\0FML\0" token is appended to the handshake. This interferes
        // with Bungee's IP forwarding, so we detect it, and remove it from the host string, for now.
        // We know FML appends \00FML\00. However, we need to also consider that other systems might
        // add their own data to the end of the string. So, we just take everything from the \0 character
        // and save it for later.
        if (packetHandshakingInSetProtocol.getHost().contains("\0")) {
            String[] split = packetHandshakingInSetProtocol.getHost().split("\0", 2);
            packetHandshakingInSetProtocol.setHost(split[0]);
            extraDataInHandshake = "\0" + split[1];
        }

        // SRV records can end with a . depending on DNS / client.
        if (packetHandshakingInSetProtocol.getHost().endsWith(".")) {
            packetHandshakingInSetProtocol.setHost(packetHandshakingInSetProtocol.getHost().substring(0, packetHandshakingInSetProtocol.getHost().length() - 1));
        }

        this.virtualHost = InetSocketAddress.createUnresolved(packetHandshakingInSetProtocol.getHost(), packetHandshakingInSetProtocol.getPort());

        switch (packetHandshakingInSetProtocol.getRequestedProtocol()) {
            case 1:
                // Ping
                thisState = State.STATUS;
                ch.setProtocol(Protocol.STATUS);
//                System.out.println("Ping: " + this);

                break;
            case 2:
                // Login
                thisState = State.USERNAME;
                ch.setProtocol(Protocol.LOGIN);
                //System.out.println("Connect: " + this);

                if (!ProtocolConstants.SUPPORTED_VERSION_IDS.contains(packetHandshakingInSetProtocol.getProtocolVersion())) {
                    disconnect("We only support 1.8");
                    return;
                }
                break;
            default:
                throw new IllegalArgumentException("Cannot request protocol " + packetHandshakingInSetProtocol.getRequestedProtocol());
        }
    }

    @Override
    public void handle(PacketLoginLoginRequest loginRequest) throws Exception {
        Preconditions.checkState(thisState == State.USERNAME, "Not expecting USERNAME");
        this.loginRequest = loginRequest;

        if (getName().contains(".")) {
            disconnect("invalid name");
            return;
        }

        if (getName().length() > 16) {
            disconnect("name too long");
            return;
        }

        this.ch.write(request = EncryptionUtil.encryptRequest());
        thisState = State.ENCRYPT;
    }

    @Override
    public void handle(final PacketLoginEncryptionResponse encryptResponse) throws Exception {
        Preconditions.checkState(thisState == State.ENCRYPT, "Not expecting ENCRYPT");

        SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);

        ch.addBefore(NetworkUtils.LENGTH_DECODER, NetworkUtils.DECRYPT, new PacketCipherDecoder(sharedKey));
        ch.addBefore(NetworkUtils.LENGTH_ENCODER, NetworkUtils.ENCRYPT, new PacketCipherEncoder(sharedKey));

        String encName = URLEncoder.encode(InitialHandler.this.getName(), "UTF-8");

        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for (byte[] bit : new byte[][]
                {
                        request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()
                }) {
            sha.update(bit);
        }
        String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");

        //String preventProxy = (getSocketAddress() instanceof InetSocketAddress) ? "&ip=" + URLEncoder.encode(getAddress().getAddress().getHostAddress(), "UTF-8") : "";
        String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encName + "&serverId=" + encodedHash;// + preventProxy;

        Callback<String> handler = (result, error) -> {
            if (error == null) {
                LoginResult obj = Util.GSON.fromJson(result, LoginResult.class);
                if (obj != null && obj.getId() != null) {
                    loginProfile = obj;
                    name = obj.getName();
                    uniqueId = Util.getUUID(obj.getId());
                    finish();
                    return;
                }
                disconnect("offline mode not supported");
            } else {
                disconnect("failed to authenticate with mojang");
            }
        };

        HttpClient.get(authURL, ch.getHandle().eventLoop(), handler);
    }

    private void finish() {
        offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(Charsets.UTF_8));
        if (uniqueId == null) {
            uniqueId = offlineId;
        }

        if (MCProxy.getInstance().getPlayerRepository().getOnlinePlayer(uniqueId) != null) {
            this.disconnect("Already connected");
            return;
        }

        ch.getHandle().eventLoop().execute(() -> {
            if (!ch.isClosing()) {
                DefaultPlayer player = new DefaultPlayer(this.proxy, new PlayerUniqueTabList(ch), ch, InitialHandler.this, 256);

                this.ch.write(new PacketPlayServerLoginSuccess(getUniqueId().toString(), getName())); // With dashes in between
                ch.setProtocol(Protocol.GAME);
                ch.getHandle().pipeline().get(HandlerEndpoint.class).setHandler(new UpstreamBridge(player));

                ServiceConnection client = MCProxy.getInstance().findBestConnection(player);

                PlayerLoginEvent event = this.proxy.getServiceRegistry().getProviderUnchecked(EventManager.class).callEvent(new PlayerLoginEvent(player, client));
                if (!this.isConnected()) {
                    return;
                }

                if (event.isCancelled()) {
                    this.disconnect(event.getCancelReason() == null ? TextComponent.fromLegacyText("§cNo reason given") : event.getCancelReason());
                    return;
                }

                client = event.getTargetConnection();
                if (client == null) {
                    this.disconnect(TextComponent.fromLegacyText("§7No client found"));
                    return;
                }

                player.useClient(client);
                thisState = State.FINISHED;
            }
        });
    }

    @Override
    public void disconnect(@NotNull String reason) {
        if (canSendKickMessage()) {
            disconnect(TextComponent.fromLegacyText(Constants.MESSAGE_PREFIX + reason));
        } else {
            ch.close();
        }
    }

    @Override
    public void disconnect(final BaseComponent... reason) {
        if (canSendKickMessage()) {
            ch.delayedClose(new PacketPlayServerKickPlayer(ComponentSerializer.toString(reason)));
        } else {
            ch.close();
        }
    }

    @Override
    public void disconnect(@NotNull BaseComponent reason) {
        disconnect(new BaseComponent[]{reason});
    }

    @Override
    public String getName() {
        return (name != null) ? name : (loginRequest == null) ? null : loginRequest.getData();
    }

    @Override
    public int getVersion() {
        return (packetHandshakingInSetProtocol == null) ? -1 : packetHandshakingInSetProtocol.getProtocolVersion();
    }

    @Override
    public @NotNull SocketAddress getSocketAddress() {
        return ch.getRemoteAddress();
    }

    @Override
    public void setOnlineMode(boolean onlineMode) {
        Preconditions.checkState(thisState == State.USERNAME, "Can only set online mode status whilst state is username");
        this.onlineMode = onlineMode;
    }

    @Override
    public void setUniqueId(UUID uuid) {
        Preconditions.checkState(thisState == State.USERNAME, "Can only set uuid while state is username");
        Preconditions.checkState(!onlineMode, "Can only set uuid when online mode is false");
        this.uniqueId = uuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        String currentName = getName();
        if (currentName != null) {
            sb.append(currentName);
            sb.append(',');
        }

        sb.append(getSocketAddress());
        sb.append("] <-> InitialHandler");

        return sb.toString();
    }

    @Override
    public boolean isConnected() {
        return !ch.isClosed();
    }

    @Override
    public void handleDisconnected(@NotNull ServiceConnection connection, @NotNull BaseComponent[] reason) {
    }
}