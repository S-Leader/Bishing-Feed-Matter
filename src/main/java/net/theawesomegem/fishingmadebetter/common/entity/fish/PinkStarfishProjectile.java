package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;
import net.theawesomegem.fishingmadebetter.registry.ModItems;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PinkStarfishProjectile extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> ITEM =
            SynchedEntityData.defineId(
                    PinkStarfishProjectile.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> ROTATION_OFFSET =
            SynchedEntityData.defineId(PinkStarfishProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Quaternionfc> IMPACT_ROTATION =
            SynchedEntityData.defineId(
                    PinkStarfishProjectile.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Boolean> HAS_IMPACT_POSE =
            SynchedEntityData.defineId(PinkStarfishProjectile.class, EntityDataSerializers.BOOLEAN);
    private final Set<UUID> hitEntities = new HashSet<>();

    public PinkStarfishProjectile(EntityType<? extends PinkStarfishProjectile> type, Level level) {
        super(type, level);

        entityData.set(ROTATION_OFFSET, random.nextInt(100));
    }

    public PinkStarfishProjectile(Level level, Player player, ItemStack stack) {
        this(ModEntities.STARFISH_PROJECTILE.get(), level);
        setOwner(player);
        setPos(player.getEyePosition().add(player.getLookAngle().scale(0.5)));
        entityData.set(ITEM, stack);
        pickup = player.getAbilities().instabuild ? Pickup.CREATIVE_ONLY : Pickup.ALLOWED;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(ITEM, ItemStack.EMPTY);
        entityData.define(ROTATION_OFFSET, 0);
        entityData.define(IMPACT_ROTATION, new Quaternionf());
        entityData.define(HAS_IMPACT_POSE, false);
    }

    public ItemStack getItem() {
        return entityData.get(ITEM).isEmpty()
                ? new ItemStack(ModItems.PINK_STARFISH_ITEM.get())
                : entityData.get(ITEM);
    }

    public boolean isInGround() {
        return super.isInGround() || entityData.get(HAS_IMPACT_POSE);
    }

    public Quaternionf getRenderRotation(float partialTick) {
        return isInGround()
                ? new Quaternionf(entityData.get(IMPACT_ROTATION))
                : flightRotation(tickCount + partialTick);
    }

    private Quaternionf flightRotation(float age) {
        Vec3 motion = getDeltaMovement();
        float yaw = (float) Math.atan2(motion.z, motion.x);
        float pitch = (float) Math.atan2(motion.y, motion.horizontalDistance());
        float tilt = (float) (90 - 20 * Math.cos((age + entityData.get(ROTATION_OFFSET)) / 10.0));
        return new Quaternionf()
                .rotateY(-yaw)
                .rotateZ(pitch)
                .rotateX(-tilt * Mth.DEG_TO_RAD)
                .rotateZ(-age);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && !super.isInGround() && entityData.get(HAS_IMPACT_POSE)) {
            entityData.set(HAS_IMPACT_POSE, false);
        }
        if (level().isClientSide() && !isInGround() && !isRemoved()) {
            level().addParticle(ModParticles.AIR_BUBBLE.get(), getX(), getY(), getZ(), 0, 0.1, 0);
        }
    }

    @Override
    public byte getPierceLevel() {
        return isInGround() ? 0 : Byte.MAX_VALUE;
    }

    @Override
    protected float getWaterInertia() {
        return 2.5F;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return getItem().copyWithCount(1);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        entityData.set(IMPACT_ROTATION, flightRotation(tickCount));
        entityData.set(HAS_IMPACT_POSE, true);
        super.onHitBlock(hit);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target != getOwner()
                && !hitEntities.contains(target.getUUID())
                && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        hitEntities.add(target.getUUID());
        if (!level().isClientSide()) {
            if (target instanceof LivingEntity && target != getOwner()) {
                target.hurtOrSimulate(damageSources().thrown(this, getOwner()), 10);
                playSound(SoundEvents.TRIDENT_HIT, 1, 1);
            }
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (tickCount > 20) super.playerTouch(player);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.store("Fish", ItemStack.CODEC, getItem());
        tag.putInt("RandomRotation", entityData.get(ROTATION_OFFSET));
        Quaternionfc rotation = entityData.get(IMPACT_ROTATION);
        CompoundTag pose = new CompoundTag();
        pose.putFloat("X", rotation.x());
        pose.putFloat("Y", rotation.y());
        pose.putFloat("Z", rotation.z());
        pose.putFloat("W", rotation.w());
        tag.store("ImpactRotation", CompoundTag.CODEC, pose);
        tag.store("HitEntities", UUIDUtil.CODEC.listOf(), hitEntities.stream().toList());
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ITEM, tag.read("Fish", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        entityData.set(ROTATION_OFFSET, tag.getIntOr("RandomRotation", 0));
        if (tag.read("ImpactRotation", CompoundTag.CODEC).isPresent()) {
            CompoundTag pose =
                    tag.read("ImpactRotation", CompoundTag.CODEC).orElseGet(CompoundTag::new);
            entityData.set(
                    IMPACT_ROTATION,
                    new Quaternionf(
                            pose.getFloatOr("X", 0F),
                            pose.getFloatOr("Y", 0F),
                            pose.getFloatOr("Z", 0F),
                            pose.getFloatOr("W", 0F)));
        } else {
            entityData.set(IMPACT_ROTATION, flightRotation(0));
        }
        entityData.set(HAS_IMPACT_POSE, super.isInGround());
        hitEntities.clear();
        hitEntities.addAll(tag.read("HitEntities", UUIDUtil.CODEC.listOf()).orElseGet(List::of));
    }
}
