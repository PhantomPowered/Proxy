package com.github.derrop.proxy.entity;

import com.github.derrop.proxy.Constants;
import com.github.derrop.proxy.api.entity.EntityPlayer;
import com.github.derrop.proxy.api.entity.PlayerInfo;
import com.github.derrop.proxy.api.entity.PlayerSkinConfiguration;
import com.github.derrop.proxy.api.network.PacketSender;
import com.github.derrop.proxy.api.network.util.PositionedPacket;
import com.github.derrop.proxy.api.service.ServiceRegistry;
import com.github.derrop.proxy.connection.ConnectedProxyClient;
import com.github.derrop.proxy.connection.cache.handler.PlayerInfoCache;
import com.github.derrop.proxy.protocol.play.server.PacketPlayServerPlayerInfo;
import com.github.derrop.proxy.protocol.play.server.entity.spawn.PacketPlayServerNamedEntitySpawn;
import com.github.derrop.proxy.util.serialize.MinecraftSerializableObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CachedPlayer extends CachedEntityWithMetadata implements EntityPlayer {

    private final UUID uniqueId;
    private final PlayerInfoCache infoCache;
    private final PlayerSkinConfiguration skinConfiguration = new DefaultPlayerSkinConfiguration() {
        @Override
        protected MinecraftSerializableObjectList getObjectList() {
            return CachedPlayer.super.objectList;
        }
    };

    public CachedPlayer(ServiceRegistry registry, ConnectedProxyClient client, PositionedPacket spawnPacket) {
        super(registry, client, spawnPacket);
        this.uniqueId = ((PacketPlayServerNamedEntitySpawn) spawnPacket).getPlayerId();
        this.infoCache = (PlayerInfoCache) client.getPacketCache().getHandler(handler -> handler instanceof PlayerInfoCache);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public @Nullable PlayerInfo getPlayerInfo() {
        return super.client.getConnection().getWorldDataProvider().getOnlinePlayer(this.uniqueId);
    }

    @Override
    public @NotNull PlayerSkinConfiguration getSkinConfiguration() {
        return this.skinConfiguration;
    }

    @Override
    public void spawn(PacketSender sender) {
        if (!this.infoCache.isCached(this.uniqueId)) {
            // NPCs might get removed out of the player list after they have been spawned, but the client doesn't spawn them without them being in the list
            PacketPlayServerPlayerInfo.Item item = infoCache.getRemovedItem(this.uniqueId);
            sender.sendPacket(new PacketPlayServerPlayerInfo(PacketPlayServerPlayerInfo.Action.ADD_PLAYER, new PacketPlayServerPlayerInfo.Item[]{item}));
            sender.sendPacket(super.spawnPacket);
            Constants.SCHEDULED_EXECUTOR_SERVICE.schedule(
                    () -> sender.sendPacket(new PacketPlayServerPlayerInfo(PacketPlayServerPlayerInfo.Action.REMOVE_PLAYER, new PacketPlayServerPlayerInfo.Item[]{item})),
                    500, TimeUnit.MILLISECONDS
            );

            super.sendMetadataAndEquipment(sender);
        } else {
            super.spawn(sender);
        }
    }
}
