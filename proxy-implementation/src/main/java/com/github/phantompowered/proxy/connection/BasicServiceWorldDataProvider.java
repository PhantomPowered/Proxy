/*
 * MIT License
 *
 * Copyright (c) derrop and derklaro
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.phantompowered.proxy.connection;

import com.github.phantompowered.proxy.api.connection.ServiceWorldDataProvider;
import com.github.phantompowered.proxy.api.entity.PlayerInfo;
import com.github.phantompowered.proxy.api.entity.types.Entity;
import com.github.phantompowered.proxy.api.entity.types.living.human.EntityPlayer;
import com.github.phantompowered.proxy.api.player.GameMode;
import com.github.phantompowered.proxy.connection.cache.handler.*;
import com.github.phantompowered.proxy.protocol.ProtocolIds;
import com.github.phantompowered.proxy.protocol.play.server.world.PacketPlayServerTimeUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicServiceWorldDataProvider implements ServiceWorldDataProvider {

    private final BasicServiceConnection connection;

    public BasicServiceWorldDataProvider(BasicServiceConnection connection) {
        this.connection = connection;
    }

    private PacketPlayServerTimeUpdate getLastTimeUpdate() {
        return (PacketPlayServerTimeUpdate) ((SimplePacketCache) this.connection.getClient().getPacketCache().getHandler(handler -> handler.getPacketIDs()[0] == ProtocolIds.ToClient.Play.UPDATE_TIME)).getLastPacket();
    }

    private GameStateCache getGameStateCache() {
        return this.connection.getClient().getPacketCache().getHandler(GameStateCache.class);
    }

    private EntityCache getEntityCache() {
        return this.connection.getClient().getPacketCache().getHandler(EntityCache.class);
    }

    private PlayerInfoCache getPlayerInfoCache() {
        return this.connection.getClient().getPacketCache().getHandler(PlayerInfoCache.class);
    }

    @Override
    public long getWorldTime() {
        PacketPlayServerTimeUpdate update = this.getLastTimeUpdate();
        return update == null ? -1 : update.getWorldTime();
    }

    @Override
    public long getTotalWorldTime() {
        PacketPlayServerTimeUpdate update = this.getLastTimeUpdate();
        return update == null ? -1 : update.getTotalWorldTime();
    }

    @Override
    public boolean isRaining() {
        return this.getGameStateCache().isRaining();
    }

    @Override
    public boolean isThundering() {
        return this.getGameStateCache().isThundering();
    }

    @Override
    public float getRainStrength() {
        return this.getGameStateCache().getRainStrength();
    }

    @Override
    public float getThunderStrength() {
        return this.getGameStateCache().getThunderStrength();
    }

    @Override
    public @NotNull GameMode getOwnGameMode() {
        GameMode gameMode = this.getGameStateCache().getGameMode();
        if (gameMode == null) {
            LoginCache cache = this.connection.getClient().getPacketCache().getHandler(LoginCache.class);
            gameMode = GameMode.getById(cache.getLastLogin().getGameMode());
        }
        if (gameMode == null) {
            gameMode = GameMode.NOT_SET;
        }
        return gameMode;
    }

    @Override
    public PlayerInfo[] getOnlinePlayers() {
        PlayerInfoCache cache = this.getPlayerInfoCache();

        return cache.getItems().stream()
                .map(cache::toPlayerInfo)
                .toArray(PlayerInfo[]::new);
    }

    @Override
    public PlayerInfo getOnlinePlayer(@NotNull UUID uniqueId) {
        PlayerInfoCache cache = this.getPlayerInfoCache();

        return cache.getItems().stream()
                .filter(item -> item.getUniqueId().equals(uniqueId))
                .findFirst()
                .map(cache::toPlayerInfo)
                .orElse(null);
    }

    @Override
    public PlayerInfo getOnlinePlayer(@NotNull String name) {
        PlayerInfoCache cache = this.getPlayerInfoCache();

        return cache.getItems().stream()
                .filter(item -> item.getUsername().equals(name))
                .findFirst()
                .map(cache::toPlayerInfo)
                .orElse(null);
    }

    @Override
    public @NotNull Collection<EntityPlayer> getPlayersInWorld() {
        return this.getEntitiesInWorld().stream()
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityPlayer) entity)
                .collect(Collectors.toList());
    }

    @Override
    public EntityPlayer getPlayerInWorld(@NotNull UUID uniqueId) {
        return this.getEntityCache().getEntities().values().stream()
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityPlayer) entity)
                .filter(player -> player.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @NotNull Collection<? extends Entity> getEntitiesInWorld() {
        return this.getEntityCache().getEntities().values();
    }

    @Override
    public Entity getEntityInWorld(int entityId) {
        return this.getEntityCache().getEntities().get(entityId);
    }

    @Override
    public @NotNull Collection<Entity> getNearbyEntities(double maxDistance, @Nullable Predicate<Entity> tester) {
        return this.getNearbyEntitiesAsStream(maxDistance, tester).collect(Collectors.toList());
    }

    @NotNull
    private Stream<? extends Entity> getNearbyEntitiesAsStream(double maxDistance, @Nullable Predicate<Entity> tester) {
        double distanceSquared = maxDistance * maxDistance;
        return this.getEntitiesInWorld().stream()
                .filter(entity -> tester == null || tester.test(entity))
                .filter(entity -> entity.getLocation().distanceSquared(this.connection.getLocation()) <= distanceSquared);
    }

    @Override
    public @NotNull Collection<EntityPlayer> getNearbyPlayers(double maxDistance) {
        return this.getNearbyEntitiesAsStream(maxDistance, entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityPlayer) entity)
                .collect(Collectors.toList());
    }
}
