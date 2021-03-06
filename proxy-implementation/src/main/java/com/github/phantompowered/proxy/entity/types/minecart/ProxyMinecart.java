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
package com.github.phantompowered.proxy.entity.types.minecart;

import com.github.phantompowered.proxy.api.block.material.Material;
import com.github.phantompowered.proxy.api.entity.EntityType;
import com.github.phantompowered.proxy.api.entity.types.minecart.Minecart;
import com.github.phantompowered.proxy.api.item.ItemStack;
import com.github.phantompowered.proxy.api.nbt.NBTTagCompound;
import com.github.phantompowered.proxy.api.network.util.PositionedPacket;
import com.github.phantompowered.proxy.api.service.ServiceRegistry;
import com.github.phantompowered.proxy.connection.ConnectedProxyClient;
import com.github.phantompowered.proxy.entity.ProxyEntity;
import com.github.phantompowered.proxy.item.ProxyItemStack;

public class ProxyMinecart extends ProxyEntity implements Minecart {

    public ProxyMinecart(ServiceRegistry registry, ConnectedProxyClient client, PositionedPacket spawnPacket, EntityType type) {
        super(registry, client, spawnPacket, type);
        this.setSize(0.98F, 0.7F);
    }

    @Override
    public int getShakingPower() {
        return this.objectList.getInt(17);
    }

    @Override
    public int getShakingDirection() {
        return this.objectList.getInt(18);
    }

    @Override
    public float getDamage() {
        return this.objectList.getFloat(19);
    }

    @Override
    public boolean displayBlock() {
        return this.objectList.getByte(22) > 0;
    }

    @Override
    public ItemStack getBlock() {
        Material material = Material.getMaterial(this.objectList.getInt(20) & 255);
        if (material == null) {
            return null;
        }

        return new ProxyItemStack(material, 1, this.objectList.getInt(20) & 65280, new NBTTagCompound());
    }

    @Override
    public int getDisplayBlockOffset() {
        return this.objectList.getInt(21);
    }
}
