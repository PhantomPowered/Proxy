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
package com.github.derrop.proxy.protocol.play.server.entity.spawn;

import com.github.derrop.proxy.api.connection.ProtocolDirection;
import com.github.derrop.proxy.api.location.Location;
import com.github.derrop.proxy.api.network.util.PositionedPacket;
import com.github.derrop.proxy.api.network.wrapper.ProtoBuf;
import com.github.derrop.proxy.protocol.ProtocolIds;
import com.github.derrop.proxy.protocol.play.server.entity.EntityPacket;
import com.github.derrop.proxy.util.PlayerPositionPacketUtil;
import com.github.derrop.proxy.util.serialize.MinecraftSerializableObjectList;
import com.github.derrop.proxy.util.serialize.SerializableObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class PacketPlayServerNamedEntitySpawn implements PositionedPacket, EntityPacket {

    private int entityId;
    private UUID playerId;
    private int x;
    private int y;
    private int z;
    private byte yaw;
    private byte pitch;
    private short currentItem;
    private Collection<SerializableObject> objects;

    public PacketPlayServerNamedEntitySpawn(int entityId, UUID playerId, int x, int y, int z, byte yaw, byte pitch, short currentItem, Collection<SerializableObject> objects) {
        this.entityId = entityId;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.currentItem = currentItem;
        this.objects = objects;
    }

    public PacketPlayServerNamedEntitySpawn(int entityId, UUID playerId, Location location, short currentItem, Collection<SerializableObject> objects) {
        this(entityId, playerId,
                PlayerPositionPacketUtil.getClientLocation(location.getX()), PlayerPositionPacketUtil.getClientLocation(location.getY()), PlayerPositionPacketUtil.getClientLocation(location.getZ()),
                PlayerPositionPacketUtil.getClientRotation(location.getYaw()), PlayerPositionPacketUtil.getClientRotation(location.getPitch()),
                currentItem, objects
        );
    }

    public PacketPlayServerNamedEntitySpawn() {
    }

    @Override
    public int getId() {
        return ProtocolIds.ToClient.Play.NAMED_ENTITY_SPAWN;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public byte getYaw() {
        return this.yaw;
    }

    public byte getPitch() {
        return this.pitch;
    }

    public short getCurrentItem() {
        return this.currentItem;
    }

    public Collection<SerializableObject> getObjects() {
        return this.objects;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setYaw(byte yaw) {
        this.yaw = yaw;
    }

    public void setPitch(byte pitch) {
        this.pitch = pitch;
    }

    public void setCurrentItem(short currentItem) {
        this.currentItem = currentItem;
    }

    public void setObjects(Collection<SerializableObject> objects) {
        this.objects = objects;
    }

    @Override
    public void read(@NotNull ProtoBuf protoBuf, @NotNull ProtocolDirection direction, int protocolVersion) {
        this.entityId = protoBuf.readVarInt();
        this.playerId = protoBuf.readUniqueId();

        this.x = protoBuf.readInt();
        this.y = protoBuf.readInt();
        this.z = protoBuf.readInt();
        this.yaw = protoBuf.readByte();
        this.pitch = protoBuf.readByte();
        this.currentItem = protoBuf.readShort();

        this.objects = MinecraftSerializableObjectList.readList(protoBuf);
    }

    @Override
    public void write(@NotNull ProtoBuf protoBuf, @NotNull ProtocolDirection direction, int protocolVersion) {
        protoBuf.writeVarInt(this.entityId);
        protoBuf.writeUniqueId(this.playerId);

        protoBuf.writeInt(this.x);
        protoBuf.writeInt(this.y);
        protoBuf.writeInt(this.z);
        protoBuf.writeByte(this.yaw);
        protoBuf.writeByte(this.pitch);
        protoBuf.writeShort(this.currentItem);

        MinecraftSerializableObjectList.writeList(protoBuf, this.objects);
    }

    @Override
    public String toString() {
        return "PacketPlayServerNamedEntitySpawn{" +
                "entityId=" + entityId +
                ", playerId=" + playerId +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", currentItem=" + currentItem +
                ", objects=" + objects +
                '}';
    }
}
