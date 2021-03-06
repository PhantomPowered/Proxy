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
package com.github.phantompowered.proxy.network.pipeline.compression;

import com.github.phantompowered.proxy.api.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class PacketCompressor extends MessageToByteEncoder<ByteBuf> {

    private final PacketCompressionHandler packetCompressionHandler;

    public PacketCompressor(int threshold) {
        this.packetCompressionHandler = new PacketCompressionHandler(threshold, true);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) throws Exception {
        int size = byteBuf.readableBytes();
        if (size < this.packetCompressionHandler.getThreshold()) {
            ByteBufUtils.writeVarInt(0, byteBuf2);
            byteBuf2.writeBytes(byteBuf);
            return;
        }

        ByteBufUtils.writeVarInt(size, byteBuf2);
        this.packetCompressionHandler.process(byteBuf, byteBuf2);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.packetCompressionHandler.end();
    }

    public void setThreshold(int threshold) {
        this.packetCompressionHandler.setThreshold(threshold);
    }
}
