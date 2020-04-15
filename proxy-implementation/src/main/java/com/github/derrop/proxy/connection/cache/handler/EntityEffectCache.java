package com.github.derrop.proxy.connection.cache.handler;

import com.github.derrop.proxy.connection.PacketConstants;
import com.github.derrop.proxy.connection.cache.CachedPacket;
import com.github.derrop.proxy.connection.cache.PacketCache;
import com.github.derrop.proxy.connection.cache.PacketCacheHandler;
import com.github.derrop.proxy.connection.cache.TimedEntityEffect;
import com.github.derrop.proxy.protocol.play.server.entity.effect.PacketPlayServerEntityEffect;
import com.github.derrop.proxy.protocol.play.server.entity.effect.PacketPlayServerRemoveEntityEffect;
import com.github.derrop.proxy.api.connection.PacketSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityEffectCache implements PacketCacheHandler {

    private Map<Integer, Map<Integer, TimedEntityEffect>> effects = new ConcurrentHashMap<>();

    @Override
    public int[] getPacketIDs() {
        return new int[]{PacketConstants.ENTITY_EFFECT, PacketConstants.REMOVE_ENTITY_EFFECT};
    }

    @Override
    public void cachePacket(PacketCache packetCache, CachedPacket newPacket) {
        if (newPacket.getDeserializedPacket() instanceof PacketPlayServerRemoveEntityEffect) {
            PacketPlayServerRemoveEntityEffect effect = (PacketPlayServerRemoveEntityEffect) newPacket.getDeserializedPacket();
            if (this.effects.containsKey(effect.getEntityId())) {
                Map<Integer, TimedEntityEffect> effects = this.effects.get(effect.getEntityId());
                effects.remove(effect.getEffectId());
                if (effects.isEmpty()) {
                    this.effects.remove(effect.getEntityId());
                }
            }
        } else if (newPacket.getDeserializedPacket() instanceof PacketPlayServerEntityEffect) {
            TimedEntityEffect effect = TimedEntityEffect.fromEntityEffect((PacketPlayServerEntityEffect) newPacket.getDeserializedPacket());

            if (!this.effects.containsKey(effect.getEntityId())) {
                this.effects.put(effect.getEntityId(), new ConcurrentHashMap<>());
            }

            this.effects.get(effect.getEntityId()).put((int) effect.getEffectId(), effect);
        }
    }

    @Override
    public void sendCached(PacketSender con) {
        for (Map<Integer, TimedEntityEffect> effects : this.effects.values()) {
            for (TimedEntityEffect effect : effects.values()) {
                PacketPlayServerEntityEffect effectPacket = effect.toEntityEffect();
                if (effectPacket != null) {
                    con.sendPacket(effectPacket);
                } else {
                    effects.remove((int) effect.getEffectId());
                    if (effects.isEmpty()) {
                        this.effects.remove(effect.getEntityId());
                    }
                }
            }
        }
    }
}
