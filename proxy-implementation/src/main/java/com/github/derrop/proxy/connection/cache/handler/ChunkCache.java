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
package com.github.derrop.proxy.connection.cache.handler;

import com.github.derrop.proxy.api.entity.player.Player;
import com.github.derrop.proxy.api.location.BlockPos;
import com.github.derrop.proxy.api.network.Packet;
import com.github.derrop.proxy.api.network.PacketSender;
import com.github.derrop.proxy.block.DefaultBlockAccess;
import com.github.derrop.proxy.block.chunk.Chunk;
import com.github.derrop.proxy.connection.cache.CachedPacket;
import com.github.derrop.proxy.connection.cache.PacketCache;
import com.github.derrop.proxy.connection.cache.PacketCacheHandler;
import com.github.derrop.proxy.protocol.ProtocolIds;
import com.github.derrop.proxy.protocol.play.server.PacketPlayServerRespawn;
import com.github.derrop.proxy.protocol.play.server.world.PacketPlayServerBlockChange;
import com.github.derrop.proxy.protocol.play.server.world.PacketPlayServerMapChunk;
import com.github.derrop.proxy.protocol.play.server.world.PacketPlayServerMapChunkBulk;
import com.github.derrop.proxy.protocol.play.server.world.PacketPlayServerMultiBlockChange;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChunkCache implements PacketCacheHandler {

    private Collection<Chunk> chunks = new CopyOnWriteArrayList<>();
    private int dimension;

    private Player connectedPlayer;

    private DefaultBlockAccess blockAccess;

    public void setBlockAccess(DefaultBlockAccess blockAccess) {
        this.blockAccess = blockAccess;
    }

    @Override
    public int[] getPacketIDs() {
        return new int[]{
                ProtocolIds.ToClient.Play.MAP_CHUNK,
                ProtocolIds.ToClient.Play.MAP_CHUNK_BULK,
                ProtocolIds.ToClient.Play.BLOCK_CHANGE,
                ProtocolIds.ToClient.Play.MULTI_BLOCK_CHANGE,
                ProtocolIds.ToClient.Play.RESPAWN
        };
    }

    @Override
    public void cachePacket(PacketCache packetCache, CachedPacket newPacket) {
        PacketPlayServerMapChunk[] data = null;
        Packet packet = newPacket.getDeserializedPacket();

        if (packet instanceof PacketPlayServerRespawn) {

            this.dimension = ((PacketPlayServerRespawn) packet).getDimension();
            this.chunks.clear();

        } else if (packet instanceof PacketPlayServerMapChunk) {

            data = new PacketPlayServerMapChunk[]{(PacketPlayServerMapChunk) packet};

        } else if (packet instanceof PacketPlayServerMapChunkBulk) {

            PacketPlayServerMapChunkBulk chunkBulk = (PacketPlayServerMapChunkBulk) packet;

            data = new PacketPlayServerMapChunk[chunkBulk.getX().length];
            for (int i = 0; i < data.length; i++) {
                data[i] = new PacketPlayServerMapChunk(chunkBulk.getX()[i], chunkBulk.getZ()[i], chunkBulk.isB(), chunkBulk.getExtracted()[i]);
            }

        } else if (packet instanceof PacketPlayServerBlockChange) {

            PacketPlayServerBlockChange blockUpdate = (PacketPlayServerBlockChange) packet;

            this.handleBlockUpdate(blockUpdate.getPos(), blockUpdate.getBlockState());

        } else if (packet instanceof PacketPlayServerMultiBlockChange) {

            PacketPlayServerMultiBlockChange multiBlockUpdate = (PacketPlayServerMultiBlockChange) packet;

            for (PacketPlayServerMultiBlockChange.BlockUpdateData updateData : multiBlockUpdate.getUpdateData()) {
                this.handleBlockUpdate(updateData.getPos(), updateData.getBlockState());
            }

        }

        if (data == null) {
            return;
        }

        for (PacketPlayServerMapChunk chunkData : data) {
            if (chunkData.getExtracted().dataLength == 0) {
                this.unload(chunkData.getX(), chunkData.getZ());
                continue;
            }

            this.load(chunkData);
        }
    }

    private void handleBlockUpdate(BlockPos pos, int newBlockState) {
        if (this.blockAccess != null) {
            this.blockAccess.handleBlockUpdate(pos, this.getBlockStateAt(pos), newBlockState);
        }

        Chunk chunk = this.getChunk(pos);
        if (chunk != null) {
            chunk.setBlockStateAt(pos.getX(), pos.getY(), pos.getZ(), newBlockState);
        }
    }

    private void load(PacketPlayServerMapChunk chunkData) {
        Chunk chunk = new Chunk();
        chunk.fillChunk(chunkData, this.dimension);
        this.chunks.add(chunk);

        if (this.blockAccess != null) {
            this.blockAccess.handleChunkLoad(chunk);
        }
    }

    private void unload(int x, int z) {
        for (Chunk chunk : this.chunks) {
            if (chunk.getX() == x && chunk.getZ() == z) {
                if (this.blockAccess != null) {
                    this.blockAccess.handleChunkUnload(chunk);
                }
                this.chunks.remove(chunk);
            }
        }
    }

    public void setBlockStateAt(BlockPos pos, int blockState) {
        Chunk chunk = this.getChunk(pos);
        if (chunk == null) {
            return;
        }
        chunk.setBlockStateAt(pos.getX(), pos.getY(), pos.getZ(), blockState);

        if (this.connectedPlayer != null) {
            this.connectedPlayer.sendPacket(new PacketPlayServerBlockChange(pos, blockState));
        }
    }

    public int getBlockStateAt(BlockPos pos) {
        Chunk chunk = this.getChunk(pos);
        if (chunk == null) {
            return -1;
        }

        return chunk.getBlockStateAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public Collection<Chunk> getChunks() {
        return this.chunks;
    }

    public Chunk getChunk(int x, int z) {
        return this.chunks.stream().filter(chunkData -> chunkData.getX() == x && chunkData.getZ() == z).findFirst().orElse(null);
    }

    public Chunk getChunk(BlockPos pos) {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public boolean isChunkLoaded(BlockPos pos) {
        return this.getChunk(pos) != null;
    }

    public int getDimension() {
        return this.dimension;
    }

    @Override
    public void sendCached(PacketSender sender) {
        // todo chunks are sometimes not displayed correctly (the client loads the chunks - you can walk on the blocks - but all blocks are invisible): until you break a block in that chunk. Now fixed?

        if (sender instanceof Player) {
            this.connectedPlayer = (Player) sender;
        }

        for (Chunk chunk : this.chunks) {
            if (chunk.getLastChunkData() == null) {
                continue;
            }
            PacketPlayServerMapChunk data = new PacketPlayServerMapChunk(chunk.getX(), chunk.getZ(), chunk.getLastChunkData().isFullChunk(), chunk.getBytes(this.dimension));
            sender.sendPacket(data);
        }
    }

    @Override
    public void onClientSwitch(Player con) {
        /*for (Chunk chunk : this.chunks) {
            ChunkData modChunk = new ChunkData(chunk.getX(), chunk.getZ(), chunk.getLastChunkData().isB(), new ChunkData.Extracted());
            modChunk.getExtracted().dataLength = 0;
            modChunk.getExtracted().data = new byte[0];
            con.sendPacket(modChunk);
        }*/
    }
}
