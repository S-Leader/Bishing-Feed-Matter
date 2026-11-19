package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class PufferfishBomb extends PortedFish {

    private static final EntityDataAccessor<Integer> PUFF_STATE = SynchedEntityData.defineId(PufferfishBomb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INFLATE_COUNTER = SynchedEntityData.defineId(PufferfishBomb.class, EntityDataSerializers.INT);
    int deflateTimer;

    private static final Predicate<LivingEntity> SCARY_MOB = (livingEntity) -> {
        if (livingEntity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return livingEntity.getMobType() != MobType.WATER && !livingEntity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "not_scary_for_pufferfish")));
    };
    static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat()
            .ignoreInvisibilityTesting().ignoreLineOfSight().selector(SCARY_MOB);

    public static final int STATE_SMALL = 0;
    public static final int STATE_MID = 1;
    public static final int STATE_FULL = 2;
    public static final int STATE_BOOM = 3;


    public static final int MID_THRESHOLD = 20;
    public static final int FULL_THRESHOLD = 60;
    public static final int WAIT_BEFORE_CHECK = 40;
    public static final int BOOM_DELAY = 40;
    public static final int TOTAL_TIME_TO_BOOM = FULL_THRESHOLD + WAIT_BEFORE_CHECK + BOOM_DELAY;

    public PufferfishBomb(EntityType<PufferfishBomb> pufferfishBombEntityEntityType, Level level) {
        super(pufferfishBombEntityEntityType, level);
        this.refreshDimensions();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(source);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PUFF_STATE, 0);
        this.entityData.define(INFLATE_COUNTER, 0);
    }

    public int getPuffState() {
        return this.entityData.get(PUFF_STATE);
    }

    public void setPuffState(int puffState) {
        this.entityData.set(PUFF_STATE, puffState);
    }

    public int getInflateCounter() {
        return this.entityData.get(INFLATE_COUNTER);
    }

    public void setInflateCounter(int counter) {
        this.entityData.set(INFLATE_COUNTER, counter);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (PUFF_STATE.equals(key)) this.refreshDimensions();
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("PuffState", this.getPuffState());
        compound.putInt("InflateCounter", this.getInflateCounter());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setPuffState(Math.min(compound.getInt("PuffState"), 3));
        this.setInflateCounter(compound.getInt("InflateCounter"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PufferfishPuffGoal(this));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            int puffState = this.getPuffState();
            int counter = this.getInflateCounter();

            if (counter > 0) {

                if (puffState == STATE_SMALL) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP);
                    this.setPuffState(STATE_MID);

                } else if (puffState == STATE_MID && counter > MID_THRESHOLD) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP);
                    this.setPuffState(STATE_FULL);

                } else if (puffState == STATE_FULL) {

                    if (counter > FULL_THRESHOLD + WAIT_BEFORE_CHECK) {
                        if (shouldExplode()) {
                            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);

                            this.setPuffState(STATE_BOOM);
                            this.playSound(SoundEvents.TNT_PRIMED);
                        } else {

                            this.setInflateCounter(0);
                            this.deflateTimer = 0;
                        }
                    }
                } else if (puffState == STATE_BOOM) {

                    if (counter == FULL_THRESHOLD + WAIT_BEFORE_CHECK + 1) {
                        this.playSound(SoundEvents.TNT_PRIMED);
                    }

                    if (counter > FULL_THRESHOLD + WAIT_BEFORE_CHECK + BOOM_DELAY) {
                        doExplode();
                        this.discard();
                    }
                }


                if (this.getInflateCounter() > 0) {
                    this.setInflateCounter(counter + 1);
                }
            } else {

                if (puffState == STATE_MID && this.deflateTimer > 100) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT);
                    this.setPuffState(STATE_SMALL);
                    this.deflateTimer = 0;
                } else if (puffState == STATE_FULL && this.deflateTimer > 100) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT);
                    this.setPuffState(STATE_MID);
                    this.deflateTimer = 0;
                } else if (puffState == STATE_BOOM) {

                    if (this.deflateTimer > BOOM_DELAY) {
                        doExplode();
                        this.discard();
                    }
                }
                ++this.deflateTimer;
            }
        }

        super.tick();
    }


    private boolean shouldExplode() {
        Player nearestPlayer = this.level().getNearestPlayer(this, 10.0);
        if (nearestPlayer != null && !nearestPlayer.isCreative() && !nearestPlayer.isSpectator()) return true;

        LivingEntity lastAttacker = this.getLastHurtByMob();
        return lastAttacker != null && lastAttacker.isAlive() && this.distanceTo(lastAttacker) <= 10.0;
    }


    private void doExplode() {
        DamageSource damageSource = this.damageSources().explosion(this, this);
        ExplosionDamageCalculator calculator = new WaterIgnoringDamageCalculator();
        this.level().explode(this, damageSource, calculator,
                this.getX(), this.getY(), this.getZ(), 5, false,
                Level.ExplosionInteraction.MOB);
    }

    public int getFuse() {
        if (this.getPuffState() == STATE_BOOM) {
            int total = FULL_THRESHOLD + WAIT_BEFORE_CHECK + BOOM_DELAY;
            int current = this.getInflateCounter();
            return Math.max(0, total - current);
        }
        return 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && this.isAlive() && this.getPuffState() > 0) {
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(0.3),
                    (p_149013_) -> targetingConditions.test(this, p_149013_))) {
                if (livingEntity.isAlive()) {
                    this.finalTouch(livingEntity);
                }
            }
        }
    }

    @Override
    public int getAirSupply() {
        return this.getPuffState() == STATE_FULL ? 300 : super.getAirSupply();
    }

    public boolean finalTouch(LivingEntity livingEntity) {
        int i = this.getPuffState();
        if (i == STATE_BOOM) i = 3;
        boolean flag = livingEntity.hurt(this.damageSources().mobAttack(this), (float) (1 + i));
        if (flag) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * i, 0), this);
            this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
        }
        return flag;
    }

    @Override
    public void playerTouch(Player player) {
        int i = this.getPuffState();
        if (i == STATE_BOOM) i = 3;
        if (player instanceof ServerPlayer && i > 0 && this.finalTouch(player)) {
            if (!this.isSilent()) {
                ((ServerPlayer) player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PUFFER_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PUFFER_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PUFFER_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.PUFFER_FISH_FLOP;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getScale(this.getPuffState()));
    }

    private static float getScale(int puffState) {
        switch (puffState) {
            case STATE_SMALL -> {
                return 0.5F;
            }
            case STATE_MID -> {
                return 0.7F;
            }
            case STATE_FULL -> {
                return 1.0F;
            }
            case STATE_BOOM -> {
                return 1.2F;
            }
            default -> {
                return 1.0F;
            }
        }
    }

    static class PufferfishPuffGoal extends Goal {
        private final PufferfishBomb fish;

        public PufferfishPuffGoal(PufferfishBomb fish) {
            this.fish = fish;
        }

        @Override
        public boolean canUse() {
            if (fish.getPuffState() == STATE_BOOM) return false;
            List<LivingEntity> list = this.fish.level().getEntitiesOfClass(LivingEntity.class,
                    this.fish.getBoundingBox().inflate(2.0),
                    (target) -> PufferfishBomb.targetingConditions.test(this.fish, target));
            return !list.isEmpty();
        }

        @Override
        public void start() {
            this.fish.setInflateCounter(1);
            this.fish.deflateTimer = 0;
        }

        @Override
        public void stop() {
            int state = this.fish.getPuffState();
            if (state < PufferfishBomb.STATE_FULL) {
                this.fish.setInflateCounter(0);
            }
        }
    }
}
