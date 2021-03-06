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
package com.github.phantompowered.proxy.protocol.play.client.position;

import com.github.phantompowered.proxy.api.connection.ProtocolDirection;
import com.github.phantompowered.proxy.api.location.Location;
import com.github.phantompowered.proxy.api.network.wrapper.ProtoBuf;
import com.github.phantompowered.proxy.protocol.ProtocolIds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketPlayClientPositionLook extends PacketPlayClientPlayerPosition {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public PacketPlayClientPositionLook(Location location) {
        super(location.isOnGround());
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public PacketPlayClientPositionLook() {
    }

    @Override
    public int getId() {
        return ProtocolIds.FromClient.Play.POSITION_LOOK;
    }

    @Override
    public void read(@NotNull ProtoBuf protoBuf, @NotNull ProtocolDirection direction, int protocolVersion) {
        this.x = protoBuf.readDouble();
        this.y = protoBuf.readDouble();
        this.z = protoBuf.readDouble();
        this.yaw = protoBuf.readFloat();
        this.pitch = protoBuf.readFloat();
        super.read(protoBuf, direction, protocolVersion);
    }

    @Override
    public void write(@NotNull ProtoBuf protoBuf, @NotNull ProtocolDirection direction, int protocolVersion) {
        protoBuf.writeDouble(this.x);
        protoBuf.writeDouble(this.y);
        protoBuf.writeDouble(this.z);
        protoBuf.writeFloat(this.yaw);
        protoBuf.writeFloat(this.pitch);
        super.write(protoBuf, direction, protocolVersion);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    @Override
    public Location getLocation(@Nullable Location before) {
        return new Location(this.x, this.y, this.z, this.yaw, this.pitch, super.isOnGround());
    }

    @Override
    public String toString() {
        return "PacketPlayClientPositionLook{"
                + "x=" + x
                + ", y=" + y
                + ", z=" + z
                + ", yaw=" + yaw
                + ", pitch=" + pitch
                + "} " + super.toString();
    }
}
