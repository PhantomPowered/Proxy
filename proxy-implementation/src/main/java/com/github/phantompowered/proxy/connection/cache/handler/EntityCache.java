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
package com.github.phantompowered.proxy.connection.cache.handler;

import com.github.phantompowered.proxy.api.APIUtil;
import com.github.phantompowered.proxy.api.event.EventManager;
import com.github.phantompowered.proxy.api.events.connection.service.entity.EntityPlayerSpawnEvent;
import com.github.phantompowered.proxy.api.item.ItemStack;
import com.github.phantompowered.proxy.api.network.Packet;
import com.github.phantompowered.proxy.api.network.PacketSender;
import com.github.phantompowered.proxy.api.network.exception.CancelProceedException;
import com.github.phantompowered.proxy.api.player.Player;
import com.github.phantompowered.proxy.api.service.ServiceRegistry;
import com.github.phantompowered.proxy.connection.ConnectedProxyClient;
import com.github.phantompowered.proxy.connection.cache.PacketCache;
import com.github.phantompowered.proxy.connection.cache.PacketCacheHandler;
import com.github.phantompowered.proxy.data.DataWatcher;
import com.github.phantompowered.proxy.entity.ProxyEntity;
import com.github.phantompowered.proxy.entity.types.ProxyExperienceOrb;
import com.github.phantompowered.proxy.entity.types.living.human.ProxyPlayer;
import com.github.phantompowered.proxy.protocol.ProtocolIds;
import com.github.phantompowered.proxy.protocol.play.server.PacketPlayServerRespawn;
import com.github.phantompowered.proxy.protocol.play.server.entity.PacketPlayServerEntityDestroy;
import com.github.phantompowered.proxy.protocol.play.server.entity.PacketPlayServerEntityEquipment;
import com.github.phantompowered.proxy.protocol.play.server.entity.PacketPlayServerEntityMetadata;
import com.github.phantompowered.proxy.protocol.play.server.entity.PacketPlayServerEntityTeleport;
import com.github.phantompowered.proxy.protocol.play.server.entity.spawn.PacketPlayServerNamedEntitySpawn;
import com.github.phantompowered.proxy.protocol.play.server.entity.spawn.PacketPlayServerSpawnEntity;
import com.github.phantompowered.proxy.protocol.play.server.entity.spawn.PacketPlayServerSpawnEntityExperienceOrb;
import com.github.phantompowered.proxy.protocol.play.server.entity.spawn.PacketPlayServerSpawnLivingEntity;
import com.github.phantompowered.proxy.protocol.play.server.player.PacketPlayServerCamera;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EntityCache implements PacketCacheHandler {

    private final Map<Integer, ProxyEntity> entities = new ConcurrentHashMap<>();
    private final DataWatcher ownMetadata = new DataWatcher();
    private int cameraTargetId = -1;
    private PacketCache packetCache;

    @Override
    public int[] getPacketIDs() {
        return new int[]{
                ProtocolIds.ToClient.Play.ENTITY_TELEPORT,
                ProtocolIds.ToClient.Play.CAMERA,
                ProtocolIds.ToClient.Play.ENTITY_METADATA,
                ProtocolIds.ToClient.Play.ENTITY_EQUIPMENT,

                ProtocolIds.ToClient.Play.NAMED_ENTITY_SPAWN,
                ProtocolIds.ToClient.Play.SPAWN_ENTITY_LIVING,
                ProtocolIds.ToClient.Play.SPAWN_ENTITY,
                ProtocolIds.ToClient.Play.SPAWN_ENTITY_EXPERIENCE_ORB,

                ProtocolIds.ToClient.Play.ENTITY_DESTROY,

                ProtocolIds.ToClient.Play.RESPAWN
        };
    }

    public Map<Integer, ProxyEntity> getEntities() {
        return this.entities;
    }

    @Override
    public void cachePacket(PacketCache packetCache, Packet newPacket) {
        this.packetCache = packetCache;
        ServiceRegistry registry = packetCache.getTargetProxyClient().getServiceRegistry();
        EventManager eventManager = registry.getProviderUnchecked(EventManager.class);

        if (newPacket instanceof PacketPlayServerRespawn) {
            this.entities.clear();
        } else if (newPacket instanceof PacketPlayServerEntityTeleport) {
            PacketPlayServerEntityTeleport teleport = (PacketPlayServerEntityTeleport) newPacket;
            if (this.entities.containsKey(teleport.getEntityId())) {
                this.entities.get(teleport.getEntityId()).updateLocation(
                        teleport.getX(), teleport.getY(), teleport.getZ(),
                        teleport.getYaw(), teleport.getPitch(),
                        teleport.isOnGround()
                );
            }
        } else if (newPacket instanceof PacketPlayServerSpawnEntityExperienceOrb) {
            PacketPlayServerSpawnEntityExperienceOrb spawn = (PacketPlayServerSpawnEntityExperienceOrb) newPacket;
            this.entities.put(spawn.getEntityId(), new ProxyExperienceOrb(registry, packetCache.getTargetProxyClient(), spawn, spawn.getXpValue()));
        } else if (newPacket instanceof PacketPlayServerNamedEntitySpawn) {
            PacketPlayServerNamedEntitySpawn spawn = (PacketPlayServerNamedEntitySpawn) newPacket;

            ProxyPlayer player = new ProxyPlayer(registry, packetCache.getTargetProxyClient(), spawn);

            if (eventManager.callEvent(new EntityPlayerSpawnEvent(packetCache.getTargetProxyClient().getConnection(), player)).isCancelled()) {
                throw CancelProceedException.INSTANCE;
            }

            this.entities.put(spawn.getEntityId(), player);
        } else if (newPacket instanceof PacketPlayServerEntityMetadata) {
            PacketPlayServerEntityMetadata metadata = (PacketPlayServerEntityMetadata) newPacket;
            if (this.entities.containsKey(metadata.getEntityId())) {
                this.entities.get(metadata.getEntityId()).getCallable().handleEntityPacket(metadata);
            } else if (metadata.getEntityId() == packetCache.getTargetProxyClient().getEntityId()) {
                this.ownMetadata.applyUpdate(metadata.getObjects());
            }
        } else if (newPacket instanceof PacketPlayServerSpawnLivingEntity) {
            PacketPlayServerSpawnLivingEntity spawn = (PacketPlayServerSpawnLivingEntity) newPacket;
            this.entities.put(spawn.getEntityId(), ProxyEntity.createEntityLiving(registry, packetCache.getTargetProxyClient(), spawn, spawn.getType()));
        } else if (newPacket instanceof PacketPlayServerSpawnEntity) {
            PacketPlayServerSpawnEntity spawn = (PacketPlayServerSpawnEntity) newPacket;
            this.entities.put(spawn.getEntityId(), ProxyEntity.createEntity(registry, packetCache.getTargetProxyClient(), spawn, spawn.getType(), spawn.getExtraData()));
        } else if (newPacket instanceof PacketPlayServerEntityDestroy) {
            PacketPlayServerEntityDestroy destroyEntities = (PacketPlayServerEntityDestroy) newPacket;
            for (int entityId : destroyEntities.getEntityIds()) {
                this.entities.remove(entityId);
            }
        } else if (newPacket instanceof PacketPlayServerEntityEquipment) {
            PacketPlayServerEntityEquipment equipment = (PacketPlayServerEntityEquipment) newPacket;
            if (this.entities.containsKey(equipment.getEntityId())) {
                ItemStack stack = this.entities.get(equipment.getEntityId()).setEquipmentSlot(equipment.getSlot(), equipment.getItem());
                if (stack == null) {
                    // TODO send the packet to the old slot back
                    throw CancelProceedException.INSTANCE;
                }

                equipment.setItem(stack);
            }
        } else if (newPacket instanceof PacketPlayServerCamera) {
            this.cameraTargetId = ((PacketPlayServerCamera) newPacket).getEntityId();
        }
    }

    @Override
    public void sendCached(PacketSender con, ConnectedProxyClient targetProxyClient) {
        if (this.packetCache == null) {
            return;
        }

        for (ProxyEntity entity : this.entities.values()) {
            entity.spawn(con);
        }

        if (this.shouldSendCameraPacket(targetProxyClient)) {
            APIUtil.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
                if (this.shouldSendCameraPacket(targetProxyClient)) {
                    con.sendPacket(new PacketPlayServerCamera(this.cameraTargetId));
                }
            }, 500, TimeUnit.MILLISECONDS);
        }

        con.sendPacket(new PacketPlayServerEntityMetadata(targetProxyClient.getEntityId(), this.ownMetadata.getObjects()));
    }

    private boolean shouldSendCameraPacket(ConnectedProxyClient targetProxyClient) {
        return this.cameraTargetId != -1 && this.cameraTargetId != targetProxyClient.getEntityId();
    }

    @Override
    public void onClientSwitch(Player con) {
        if (this.entities.isEmpty()) {
            return;
        }

        Set<Integer> entityIdSet = new HashSet<>(this.entities.keySet());

        int[] entityIds = new int[entityIdSet.size()];
        int i = 0;
        for (Integer entityId : entityIdSet) {
            entityIds[i++] = entityId;
        }

        con.sendPacket(new PacketPlayServerEntityDestroy(entityIds));
    }
}
