package com.github.phantompowered.proxy.entity;

import com.github.phantompowered.proxy.api.entity.EntityEffect;
import com.github.phantompowered.proxy.api.entity.EntityType;
import com.github.phantompowered.proxy.api.entity.LivingEntityType;
import com.github.phantompowered.proxy.api.entity.types.Entity;
import com.github.phantompowered.proxy.api.entity.types.SpawnedEntity;
import com.github.phantompowered.proxy.api.event.EventManager;
import com.github.phantompowered.proxy.api.events.connection.service.EquipmentSlotChangeEvent;
import com.github.phantompowered.proxy.api.item.ItemStack;
import com.github.phantompowered.proxy.api.location.Location;
import com.github.phantompowered.proxy.api.location.object.MathObject;
import com.github.phantompowered.proxy.api.network.Packet;
import com.github.phantompowered.proxy.api.network.PacketSender;
import com.github.phantompowered.proxy.api.network.util.PositionedPacket;
import com.github.phantompowered.proxy.api.player.inventory.EquipmentSlot;
import com.github.phantompowered.proxy.api.service.ServiceRegistry;
import com.github.phantompowered.proxy.connection.ConnectedProxyClient;
import com.github.phantompowered.proxy.connection.cache.TimedEntityEffect;
import com.github.phantompowered.proxy.connection.cache.handler.EntityEffectCache;
import com.github.phantompowered.proxy.data.DataWatcher;
import com.github.phantompowered.proxy.entity.types.block.ProxyEnderCrystal;
import com.github.phantompowered.proxy.entity.types.block.ProxyFallingBlock;
import com.github.phantompowered.proxy.entity.types.block.ProxyTNTPrimed;
import com.github.phantompowered.proxy.entity.types.item.ProxyArrow;
import com.github.phantompowered.proxy.entity.types.item.ProxyEgg;
import com.github.phantompowered.proxy.entity.types.item.ProxyEnderPearl;
import com.github.phantompowered.proxy.entity.types.item.ProxyEnderSignal;
import com.github.phantompowered.proxy.entity.types.item.ProxyFireball;
import com.github.phantompowered.proxy.entity.types.item.ProxyFishingHook;
import com.github.phantompowered.proxy.entity.types.item.ProxyItem;
import com.github.phantompowered.proxy.entity.types.item.ProxyItemFrame;
import com.github.phantompowered.proxy.entity.types.item.ProxyLeash;
import com.github.phantompowered.proxy.entity.types.item.ProxyPotion;
import com.github.phantompowered.proxy.entity.types.item.ProxySmallFireball;
import com.github.phantompowered.proxy.entity.types.item.ProxySnowball;
import com.github.phantompowered.proxy.entity.types.item.ProxyThrownExpBottle;
import com.github.phantompowered.proxy.entity.types.item.ProxyWitherSkull;
import com.github.phantompowered.proxy.entity.types.living.ProxyEntityLiving;
import com.github.phantompowered.proxy.entity.types.living.animal.ProxyBat;
import com.github.phantompowered.proxy.entity.types.living.animal.ProxySquid;
import com.github.phantompowered.proxy.entity.types.living.animal.ageable.ProxyChicken;
import com.github.phantompowered.proxy.entity.types.living.animal.ageable.ProxyCow;
import com.github.phantompowered.proxy.entity.types.living.animal.ageable.ProxyHorse;
import com.github.phantompowered.proxy.entity.types.living.animal.ageable.ProxyMushroomCow;
import com.github.phantompowered.proxy.entity.types.living.animal.ageable.ProxyPig;
import com.github.phantompowered.proxy.entity.types.living.animal.ageable.ProxyRabbit;
import com.github.phantompowered.proxy.entity.types.living.animal.ageable.ProxySheep;
import com.github.phantompowered.proxy.entity.types.living.animal.npc.ProxyVillager;
import com.github.phantompowered.proxy.entity.types.living.animal.tamable.ProxyOcelot;
import com.github.phantompowered.proxy.entity.types.living.animal.tamable.ProxyWolf;
import com.github.phantompowered.proxy.entity.types.living.boss.ProxyEnderDragon;
import com.github.phantompowered.proxy.entity.types.living.boss.ProxyWither;
import com.github.phantompowered.proxy.entity.types.living.creature.ProxyIronGolem;
import com.github.phantompowered.proxy.entity.types.living.creature.ProxySnowman;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyBlaze;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyCaveSpider;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyCreeper;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyEnderman;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyEndermite;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyGhast;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyGiantZombie;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyLavaSlime;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyPigZombie;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxySilverfish;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxySkeleton;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxySlime;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxySpider;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyWitch;
import com.github.phantompowered.proxy.entity.types.living.monster.ProxyZombie;
import com.github.phantompowered.proxy.entity.types.minecart.ProxyCommandBlockMinecart;
import com.github.phantompowered.proxy.entity.types.minecart.ProxyFurnaceMinecart;
import com.github.phantompowered.proxy.entity.types.minecart.ProxyMinecart;
import com.github.phantompowered.proxy.logging.ProxyLogger;
import com.github.phantompowered.proxy.protocol.play.server.entity.PacketPlayServerEntityEquipment;
import com.github.phantompowered.proxy.protocol.play.server.entity.PacketPlayServerEntityMetadata;
import com.github.phantompowered.proxy.protocol.play.server.entity.effect.PacketPlayServerRemoveEntityEffect;
import com.github.phantompowered.proxy.protocol.play.server.entity.spawn.PacketPlayServerNamedEntitySpawn;
import com.github.phantompowered.proxy.protocol.play.server.entity.util.PlayerPositionPacketUtil;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyEntity extends ProxyScaleable implements SpawnedEntity, Entity.Callable {

    protected final ServiceRegistry registry;
    protected final ConnectedProxyClient client;
    protected final Map<Integer, ItemStack> equipment;
    protected final DataWatcher objectList = new DataWatcher();
    private final int entityId;
    private final Object type;
    protected Location location;
    protected @Nullable PositionedPacket packet;
    private boolean onGround;

    private final Map<String, Object> properties = new HashMap<>();
    private final Unsafe unsafe = this::teleport;

    public ProxyEntity(ServiceRegistry registry, ConnectedProxyClient client, PositionedPacket spawnPacket, Object type) {
        this(registry, client, new Location(
                PlayerPositionPacketUtil.getServerLocation(spawnPacket.getX()),
                PlayerPositionPacketUtil.getServerLocation(spawnPacket.getY()),
                PlayerPositionPacketUtil.getServerLocation(spawnPacket.getZ()),
                PlayerPositionPacketUtil.getServerRotation(spawnPacket.getYaw()),
                PlayerPositionPacketUtil.getServerRotation(spawnPacket.getPitch())
        ), spawnPacket.getEntityId(), type);

        this.packet = spawnPacket;
    }

    protected ProxyEntity(ServiceRegistry registry, ConnectedProxyClient client, Location location, int entityId, Object type) {
        super(0.6F, 1.8F);
        this.registry = registry;
        this.client = client;
        this.entityId = entityId;
        this.type = type;
        this.location = location;
        this.equipment = new ConcurrentHashMap<>();
    }

    public static ProxyEntity createEntityLiving(ServiceRegistry registry, ConnectedProxyClient client, PositionedPacket spawnPacket, int type) {
        LivingEntityType entityType = LivingEntityType.fromId(type);
        Preconditions.checkNotNull(entityType, "Unable to create entity type from unknown id " + type);

        switch (entityType) {
            case BAT:
                return new ProxyBat(registry, client, spawnPacket);
            case HORSE:
                return new ProxyHorse(registry, client, spawnPacket);
            case OCELOT:
                return new ProxyOcelot(registry, client, spawnPacket);
            case WOLF:
                return new ProxyWolf(registry, client, spawnPacket);
            case PIG:
                return new ProxyPig(registry, client, spawnPacket);
            case RABBIT:
                return new ProxyRabbit(registry, client, spawnPacket);
            case SHEEP:
                return new ProxySheep(registry, client, spawnPacket);
            case VILLAGER:
                return new ProxyVillager(registry, client, spawnPacket);
            case ENDER_MAN:
                return new ProxyEnderman(registry, client, spawnPacket);
            case ZOMBIE:
                return new ProxyZombie(registry, client, spawnPacket, entityType);
            case PIG_ZOMBIE:
                return new ProxyPigZombie(registry, client, spawnPacket);
            case BLAZE:
                return new ProxyBlaze(registry, client, spawnPacket);
            case SPIDER:
                return new ProxySpider(registry, client, spawnPacket, entityType);
            case CAVE_SPIDER:
                return new ProxyCaveSpider(registry, client, spawnPacket);
            case CREEPER:
                return new ProxyCreeper(registry, client, spawnPacket);
            case GHAST:
                return new ProxyGhast(registry, client, spawnPacket);
            case SLIME:
                return new ProxySlime(registry, client, spawnPacket, entityType);
            case LAVA_SLIME:
                return new ProxyLavaSlime(registry, client, spawnPacket);
            case SKELETON:
                return new ProxySkeleton(registry, client, spawnPacket);
            case WITCH:
                return new ProxyWitch(registry, client, spawnPacket);
            case WITHER:
                return new ProxyWither(registry, client, spawnPacket);
            case GUARDIAN:
                return new ProxyGuardian(registry, client, spawnPacket);
            case ENDER_DRAGON:
                return new ProxyEnderDragon(registry, client, spawnPacket);
            case SNOW_MAN:
                return new ProxySnowman(registry, client, spawnPacket);
            case VILLAGER_GOLEM:
                return new ProxyIronGolem(registry, client, spawnPacket);
            case GIANT:
                return new ProxyGiantZombie(registry, client, spawnPacket);
            case SILVER_FISH:
                return new ProxySilverfish(registry, client, spawnPacket);
            case COW:
                return new ProxyCow(registry, client, spawnPacket, entityType);
            case CHICKEN:
                return new ProxyChicken(registry, client, spawnPacket);
            case SQUID:
                return new ProxySquid(registry, client, spawnPacket);
            case MUSHROOM_COW:
                return new ProxyMushroomCow(registry, client, spawnPacket);
            case ENDER_MITE:
                return new ProxyEndermite(registry, client, spawnPacket);
            default:
                registry.getProviderUnchecked(ProxyLogger.class).warning(String.format("Unable to create correct living entity type for type id %d", type));
                return new ProxyEntityLiving(registry, client, spawnPacket, entityType);
        }
    }

    public static ProxyEntity createEntity(ServiceRegistry serviceRegistry, ConnectedProxyClient client, PositionedPacket spawnPacket, int type, int subType) {
        EntityType entityType = EntityType.fromId(type, subType);
        Preconditions.checkNotNull(entityType, "Cannot create entity from type id " + type + " with sub id " + subType);

        switch (entityType) {
            case ARMOR_STAND:
                return new ProxyArmorStand(serviceRegistry, client, spawnPacket);
            case BOAT:
                return new ProxyBoat(serviceRegistry, client, spawnPacket);
            case ITEM:
                return new ProxyItem(serviceRegistry, client, spawnPacket);
            case ARROW:
                return new ProxyArrow(serviceRegistry, client, subType, spawnPacket);
            case FIREWORK:
                return new ProxyFirework(serviceRegistry, client, spawnPacket);
            case ITEM_FRAME:
                return new ProxyItemFrame(serviceRegistry, client, spawnPacket);
            case ENDER_CRYSTAL:
                return new ProxyEnderCrystal(serviceRegistry, client, spawnPacket);
            case EGG:
                return new ProxyEgg(serviceRegistry, client, spawnPacket);
            case LEASH:
                return new ProxyLeash(serviceRegistry, client, spawnPacket);
            case POTION:
                return new ProxyPotion(serviceRegistry, client, spawnPacket, subType);
            case SNOWBALL:
                return new ProxySnowball(serviceRegistry, client, spawnPacket);
            case FIRE_BALL:
                return new ProxyFireball(serviceRegistry, client, spawnPacket, entityType, subType);
            case TNT_PRIMED:
                return new ProxyTNTPrimed(serviceRegistry, client, spawnPacket);
            case ENDER_PEARL:
                return new ProxyEnderPearl(serviceRegistry, client, spawnPacket);
            case ENDER_SIGNAL:
                return new ProxyEnderSignal(serviceRegistry, client, spawnPacket);
            case FISHING_HOOK:
                return new ProxyFishingHook(serviceRegistry, client, spawnPacket, subType);
            case WITHER_SKULL:
                return new ProxyWitherSkull(serviceRegistry, client, spawnPacket, subType);
            case FALLING_BLOCK:
                return new ProxyFallingBlock(serviceRegistry, client, spawnPacket, subType);
            case FURNACE_MINE_CART:
                return new ProxyFurnaceMinecart(serviceRegistry, client, spawnPacket);
            case COMMAND_BLOCK_MINE_CART:
                return new ProxyCommandBlockMinecart(serviceRegistry, client, spawnPacket);
            case MOB_SPAWNER_MINE_CART:
            case CHEST_MINE_CART:
            case EMPTY_MINE_CART:
            case HOPPER_MINE_CART:
            case TNT_MINE_CART:
                return new ProxyMinecart(serviceRegistry, client, spawnPacket, entityType);
            case SMALL_FIRE_BALL:
                return new ProxySmallFireball(serviceRegistry, client, spawnPacket, subType);
            case THROWN_EXP_BOTTLE:
                return new ProxyThrownExpBottle(serviceRegistry, client, spawnPacket);
            default:
                serviceRegistry.getProviderUnchecked(ProxyLogger.class).warning(String.format("Unable to create correct entity type for type id %d (sub: %d)", type, subType));
                return new ProxyEntity(serviceRegistry, client, spawnPacket, entityType);
        }
    }

    @Override
    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public int getDimension() {
        return this.client.getDimension();
    }

    @Override
    public @NotNull Unsafe unsafe() {
        return this.unsafe;
    }

    @Override
    public @NotNull Callable getCallable() {
        return this;
    }

    @Override
    public float getHeadHeight() {
        return this.length * 0.85F;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) this.properties.get(key);
    }

    @Override
    public <T> void setProperty(String key, T value) {
        this.properties.put(key, value);
    }

    @Override
    public void removeProperty(String key) {
        this.properties.remove(key);
    }

    @NotNull
    @Override
    public MathObject getBoundingBox() {
        return MathObject.cuboidFromBottomCenter(this.getLocation(), this.getWidth(), this.getLength());
    }

    @Override
    public boolean isBurning() {
        return this.getFlag(0);
    }

    @Override
    public boolean isSneaking() {
        return this.getFlag(1);
    }

    @Override
    public boolean isRiding() {
        return this.getFlag(2);
    }

    @Override
    public boolean isSprinting() {
        return this.getFlag(3);
    }

    @Override
    public boolean isEating() {
        return this.getFlag(4);
    }

    @Override
    public boolean isInvisible() {
        return this.getFlag(5);
    }

    @Override
    public boolean getFlag(int flag) {
        return (this.objectList.getByte(0) & 1 << flag) != 0;
    }

    @Override
    public short getAirTicks() {
        return this.objectList.getShort(1);
    }

    @Override
    public boolean isCustomNameVisible() {
        return this.objectList.getByte(3) == 1;
    }

    @Override
    public boolean isSilent() {
        return this.objectList.getByte(4) == 1;
    }

    @Override
    public boolean hasCustomName() {
        String s = this.objectList.getString(2);
        return s != null && s.length() > 0;
    }

    @Override
    public String getCustomName() {
        return this.objectList.getString(2);
    }

    @Override
    public boolean isLiving() {
        return this.type instanceof LivingEntityType;
    }

    @Override
    public EntityType getType() {
        return this.type instanceof EntityType ? (EntityType) this.type : null;
    }

    @Override
    public @Nullable LivingEntityType getLivingType() {
        return this.type instanceof LivingEntityType ? (LivingEntityType) this.type : null;
    }

    @Override
    public boolean isOfType(@Nullable EntityType type) {
        return this.type.equals(type);
    }

    @Override
    public boolean isOfType(@Nullable LivingEntityType type) {
        return this.type.equals(type);
    }

    @NotNull
    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public @NotNull Location getHeadLocation() {
        return this.location.clone().add(0, this.getHeadHeight(), 0);
    }

    @Override
    public void teleport(@NotNull Location location) {
        this.location = location;
        this.updateLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.isOnGround());
    }

    public void updateLocation(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.updateLocation(
                PlayerPositionPacketUtil.getClientLocation(x),
                PlayerPositionPacketUtil.getClientLocation(y),
                PlayerPositionPacketUtil.getClientLocation(z),
                PlayerPositionPacketUtil.getClientRotation(yaw),
                PlayerPositionPacketUtil.getClientRotation(pitch),
                onGround
        );
    }

    public void updateLocation(int x, int y, int z, byte yaw, byte pitch, boolean onGround) {
        if (this.packet == null) {
            return;
        }

        this.packet.setX(x);
        this.packet.setY(y);
        this.packet.setZ(z);
        this.packet.setYaw(yaw);
        this.packet.setPitch(pitch);
        this.onGround = onGround;
    }

    public ItemStack setEquipmentSlot(int slotId, ItemStack item) {
        EquipmentSlot slot = EquipmentSlot.getById(slotId);
        if (slot != null) {
            EquipmentSlotChangeEvent event = this.registry.getProviderUnchecked(EventManager.class)
                    .callEvent(new EquipmentSlotChangeEvent(this.client.getConnection(), this, slot, item));

            if (event.isCancelled()) {
                return null;
            }

            this.equipment.put(slotId, item);
            return event.getItem();
        }

        return null;
    }

    public boolean isPlayer() {
        return this.packet != null && this.packet instanceof PacketPlayServerNamedEntitySpawn; // klaro - pail
    }

    public void spawn(PacketSender sender) {
        if (this.packet == null) {
            return;
        }

        sender.sendPacket(this.packet);
        this.sendEntityData(sender);
    }

    protected void sendEntityData(PacketSender sender) {
        sender.sendPacket(new PacketPlayServerEntityMetadata(this.entityId, this.objectList.getObjects()));
        this.equipment.forEach((slot, item) -> sender.sendPacket(new PacketPlayServerEntityEquipment(this.entityId, slot, item)));
    }

    @Override
    public EntityEffect createEffect(byte effectId, byte amplifier, int duration, boolean hideParticles) {
        return TimedEntityEffect.create(this.entityId, effectId, amplifier, duration, hideParticles);
    }

    private EntityEffectCache getEffectCache() {
        return this.client.getPacketCache().getHandler(EntityEffectCache.class);
    }

    @Override
    public EntityEffect[] getActiveEffects() {
        return this.getEffectCache().getEffects(this.entityId).values().toArray(new EntityEffect[0]);
    }

    @Override
    public void addEffect(EntityEffect effect) {
        TimedEntityEffect timed = (TimedEntityEffect) effect;
        if (timed.getEntityId() != this.entityId) {
            throw new IllegalArgumentException("Cannot give entity an effect which was created for another entity");
        }

        this.getEffectCache().getEffects(this.entityId).put(effect.getEffectId(), timed);
        if (this.client.getRedirector() != null) {
            this.client.getRedirector().sendPacket(timed.toEntityEffect());
        }
    }

    @Override
    public void removeEffect(byte effectId) {
        if (this.getEffectCache().getEffects(this.entityId).remove(effectId) != null && this.client.getRedirector() != null) {
            this.client.getRedirector().sendPacket(new PacketPlayServerRemoveEntityEffect(this.entityId, effectId));
        }
    }

    @Override
    public void handleEntityPacket(@NotNull Packet packet) {
        if (packet instanceof PacketPlayServerEntityMetadata) {
            PacketPlayServerEntityMetadata metadata = (PacketPlayServerEntityMetadata) packet;
            this.objectList.applyUpdate(metadata.getObjects());
        }
    }
}
