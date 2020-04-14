package com.github.derrop.proxy.connection.cache.handler;

import com.github.derrop.proxy.Constants;
import com.github.derrop.proxy.api.connection.PacketSender;
import com.github.derrop.proxy.api.entity.player.Player;
import com.github.derrop.proxy.connection.cache.CachedPacket;
import com.github.derrop.proxy.connection.cache.PacketCache;
import com.github.derrop.proxy.connection.cache.PacketCacheHandler;
import net.md_5.bungee.protocol.packet.EntityStatus;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.Respawn;

public class LoginCache implements PacketCacheHandler {

    private Login lastLogin;

    @Override
    public int[] getPacketIDs() {
        return new int[]{1}; // login packet
    }

    @Override
    public void cachePacket(PacketCache packetCache, CachedPacket newPacket) {
        if (!(newPacket.getDeserializedPacket() instanceof Login)) {
            return;
        }
        this.lastLogin = (Login) newPacket.getDeserializedPacket();
    }

    @Override
    public void sendCached(PacketSender con) {
        if (this.lastLogin == null) {
            Constants.EXECUTOR_SERVICE.execute(() -> {
                int count = 0;
                while (this.lastLogin == null && count++ < 50) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }

                if (count >= 50) {
                    return;
                }
                this.sendCached(con);
            });
            return;
        }

        if (con instanceof Player) {
            Player player = (Player) con;
            if (player.getConnectedClient() == null) {
                Login login = new Login(
                        this.lastLogin.getEntityId(),
                        (short) 0,
                        this.lastLogin.getDimension(),
                        0,
                        this.lastLogin.getDifficulty(),
                        (short) 255,
                        this.lastLogin.getLevelType(),
                        this.lastLogin.getViewDistance(),
                        this.lastLogin.isReducedDebugInfo(),
                        this.lastLogin.isNormalRespawn()
                );
                player.sendPacket(login);
                player.setDimension(this.lastLogin.getDimension());

                return;
            }

            EntityStatus entityStatus = new EntityStatus(
                    player.getEntityId(),
                    this.lastLogin.isReducedDebugInfo() ? EntityStatus.DEBUG_INFO_REDUCED : EntityStatus.DEBUG_INFO_NORMAL
            );
            player.sendPacket(entityStatus);
            player.setDimensionChange(true);
        }

        if (!(con instanceof Player) || this.lastLogin.getDimension() == ((Player) con).getDimension()) {
            con.sendPacket(new Respawn(
                    (this.lastLogin.getDimension() >= 0 ? -1 : 0),
                    this.lastLogin.getSeed(),
                    this.lastLogin.getDifficulty(),
                    this.lastLogin.getGameMode(),
                    this.lastLogin.getLevelType()
            ));
        }

        con.sendPacket(new Respawn(
                this.lastLogin.getDimension(),
                this.lastLogin.getSeed(),
                this.lastLogin.getDifficulty(),
                this.lastLogin.getGameMode(),
                this.lastLogin.getLevelType()
        ));
        if (con instanceof Player) {
            ((Player) con).setDimension(this.lastLogin.getDimension());
        }

    }

}
