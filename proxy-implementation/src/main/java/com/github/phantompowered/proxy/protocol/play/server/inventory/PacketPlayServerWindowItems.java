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
package com.github.phantompowered.proxy.protocol.play.server.inventory;

import com.github.phantompowered.proxy.api.connection.ProtocolDirection;
import com.github.phantompowered.proxy.api.item.ItemStack;
import com.github.phantompowered.proxy.api.network.Packet;
import com.github.phantompowered.proxy.api.network.wrapper.ProtoBuf;
import com.github.phantompowered.proxy.protocol.ProtocolIds;
import org.jetbrains.annotations.NotNull;

public class PacketPlayServerWindowItems implements Packet {

    private byte windowId;
    private ItemStack[] items;

    public PacketPlayServerWindowItems(byte windowId, ItemStack[] items) {
        this.windowId = windowId;
        this.items = items;
    }

    public PacketPlayServerWindowItems() {
    }

    @Override
    public int getId() {
        return ProtocolIds.ToClient.Play.WINDOW_ITEMS;
    }

    public byte getWindowId() {
        return this.windowId;
    }

    public ItemStack[] getItems() {
        return this.items;
    }

    @Override
    public void read(@NotNull ProtoBuf protoBuf, @NotNull ProtocolDirection direction, int protocolVersion) {
        this.windowId = protoBuf.readByte();
        this.items = new ItemStack[protoBuf.readShort()];

        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = protoBuf.readItemStack();
        }
    }

    @Override
    public void write(@NotNull ProtoBuf protoBuf, @NotNull ProtocolDirection direction, int protocolVersion) {
        protoBuf.writeByte(this.windowId);
        protoBuf.writeShort(this.items.length);

        for (ItemStack item : this.items) {
            protoBuf.writeItemStack(item);
        }
    }

    public String toString() {
        return "PacketPlayServerWindowItems(windowId=" + this.getWindowId() + ", items=" + java.util.Arrays.deepToString(this.getItems()) + ")";
    }
}
