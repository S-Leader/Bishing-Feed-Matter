package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.theawesomegem.fishingmadebetter.common.item.fish.ElectricEelItem;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;
import net.theawesomegem.fishingmadebetter.registry.ModSounds;

public class ElectricEelWhip extends Entity {
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(ElectricEelWhip.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FOIL = SynchedEntityData.defineId(ElectricEelWhip.class, EntityDataSerializers.BOOLEAN);
    private ItemStack weapon = ItemStack.EMPTY;

    public ElectricEelWhip(EntityType<? extends ElectricEelWhip> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public ElectricEelWhip(Level level, Player player, ItemStack stack) {
        this(ModEntities.EEL_WHIP.get(), level);
        entityData.set(OWNER, player.getId());
        entityData.set(FOIL, stack.hasFoil());
        weapon = stack.copy();
        setPos(player.getEyePosition());
    }

    public Player getOwner() {
        return level().getEntity(entityData.get(OWNER)) instanceof Player player ? player : null;
    }

    public Player getPlayerOwner() {
        return getOwner();
    }

    public boolean isFoil() {
        return entityData.get(FOIL);
    }

    public float getAnimationTicks(float partialTicks) {
        return tickCount + partialTicks;
    }

    public int getLifespan() {
        return 12;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER, -1);
        entityData.define(FOIL, false);
    }

    @Override
    public void tick() {
        super.tick();
        Player player = getOwner();
        if (player != null) setPos(player.getEyePosition());
        if (level().isClientSide) return;
        if (player == null || !player.isAlive() || !player.getMainHandItem().is(weapon.getItem()) || tickCount >= getLifespan()) {
            discard();
            return;
        }
        if (tickCount == getLifespan() / 2) {
            Vec3 start = player.getEyePosition();
            Vec3 end = start.add(player.getLookAngle().scale(10));
            end = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
            for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(1))) {
                if (target == player || target.isAlliedTo(player) || !target.isAttackable()
                        || target instanceof Player other && !player.canHarmPlayer(other)) continue;
                AABB bounds = target.getBoundingBox().inflate(0.5);
                if (!bounds.contains(start) && bounds.clip(start, end).isEmpty()) continue;
                float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) + EnchantmentHelper.getDamageBonus(weapon, target.getMobType());
                if (target.hurt(damageSources().playerAttack(player), damage)) {
                    weapon.getItem().hurtEnemy(weapon, target, player);
                    EnchantmentHelper.doPostHurtEffects(target, player);
                    EnchantmentHelper.doPostDamageEffects(player, target);
                    float knockback = EnchantmentHelper.getKnockbackBonus(player);
                    target.knockback(knockback * 0.5, Mth.sin(player.getYRot() * Mth.DEG_TO_RAD), -Mth.cos(player.getYRot() * Mth.DEG_TO_RAD));
                }
            }
            playSound(ModSounds.EEL_RELEASE.get(), 1, 1);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
