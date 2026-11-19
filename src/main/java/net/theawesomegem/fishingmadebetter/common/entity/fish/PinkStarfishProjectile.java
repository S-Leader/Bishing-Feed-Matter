package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;
import net.theawesomegem.fishingmadebetter.registry.ModItems;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PinkStarfishProjectile extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(PinkStarfishProjectile.class, EntityDataSerializers.ITEM_STACK);
    private final Set<UUID> hitEntities = new HashSet<>();

    public PinkStarfishProjectile(EntityType<? extends PinkStarfishProjectile> type, Level level) {
        super(type, level);
        setPierceLevel(Byte.MAX_VALUE);
    }

    public PinkStarfishProjectile(Level level, Player player, ItemStack stack) {
        this(ModEntities.STARFISH_PROJECTILE.get(), level);
        setOwner(player);
        setPos(player.getEyePosition().add(player.getLookAngle().scale(0.5)));
        entityData.set(ITEM, stack);
        pickup = player.getAbilities().instabuild ? Pickup.CREATIVE_ONLY : Pickup.ALLOWED;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        return entityData.get(ITEM).isEmpty() ? new ItemStack(ModItems.PINK_STARFISH_ITEM.get()) : entityData.get(ITEM);
    }

    public boolean isInGround() {
        return inGround;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && !inGround && !isRemoved()) {
            level().addParticle(ModParticles.AIR_BUBBLE.get(), getX(), getY(), getZ(), 0, 0.1, 0);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return getItem().copyWithCount(1);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target != getOwner() && !hitEntities.contains(target.getUUID()) && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        hitEntities.add(target.getUUID());
        if (!level().isClientSide) {
            if (target instanceof LivingEntity && target != getOwner()) {
                target.hurt(damageSources().thrown(this, getOwner()), 10);
                playSound(SoundEvents.TRIDENT_HIT, 1, 1);
            }
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (tickCount > 20) super.playerTouch(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Fish", getItem().save(new CompoundTag()));
        ListTag targets = new ListTag();
        hitEntities.forEach(id -> targets.add(NbtUtils.createUUID(id)));
        tag.put("HitEntities", targets);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ITEM, ItemStack.of(tag.getCompound("Fish")));
        hitEntities.clear();
        for (var id : tag.getList("HitEntities", 11)) hitEntities.add(NbtUtils.loadUUID(id));
        if (!inGround) setPierceLevel(Byte.MAX_VALUE);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
