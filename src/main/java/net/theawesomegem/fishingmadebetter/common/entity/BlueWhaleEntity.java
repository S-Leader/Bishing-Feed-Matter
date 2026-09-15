package net.theawesomegem.fishingmadebetter.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.theawesomegem.fishingmadebetter.FishingMadeBetterForge;
import net.theawesomegem.fishingmadebetter.common.config.FmbCommonConfig;
import net.theawesomegem.fishingmadebetter.mixins.ThrownTridentAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public final class BlueWhaleEntity extends WaterAnimal {
    public static final int ACTION_IDLE = 0;
    public static final int ACTION_BLOW = 1;
    public static final int ACTION_RAM = 2;
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(BlueWhaleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BEACHED = SynchedEntityData.defineId(BlueWhaleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TRIDENT_COUNT = SynchedEntityData.defineId(BlueWhaleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> STUCK_PROJECTILES = SynchedEntityData.defineId(BlueWhaleEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Boolean> STUNNED = SynchedEntityData.defineId(BlueWhaleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int BLOW_DURATION = 32;
    private static final int RAM_DURATION = 24;
    private static final int AIR_CONSUMPTION_INTERVAL = 100;
    private static final int STUN_DURATION = 60;
    /** 与海豚同值：离水 2400 tick（2 分钟）后开始脱水掉血。 */
    private static final int TOTAL_MOISTNESS = 2400;
    private static final float DRY_OUT_DAMAGE = 1.0F;
    /** 搁浅时玩家推动的助力倍率，用来抵消上岸后的强阻尼。 */
    private static final double BEACHED_PUSH_ASSIST = 2.0D;
    private static final int MAX_STUCK_TRIDENTS = 8;
    private static final int MAX_STUCK_ARROWS = 16;
    /** 拔三叉戟时玩家眼睛到插着位置的最大距离。 */
    private static final double PULL_REACH = 6.0D;
    private static final float OBSIDIAN_HARDNESS = 50.0F;
    private static final double OBSTACLE_PROBE_DISTANCE = 4.5D;
    private static final int YAW_HISTORY_SIZE = 32;
    private static final int YAW_HISTORY_MASK = YAW_HISTORY_SIZE - 1;
    /** 每格身长对应的偏航滞后 tick 数，越大身体越软。 */
    private static final float YAW_LAG_PER_BLOCK = 1.1F;
    private static final double SPINE_STEP = 0.5D;
    private static final double[] PART_OFFSETS = {1.8D, 0.2D, -1.65D, -3.0D, -4.2D, -5.35D};
    private static final double[] PART_HEIGHT_OFFSETS = {0.05D, 0.0D, 0.0D, 0.05D, 0.1D, 0.2D};

    public final BlueWhalePart headPart;
    public final BlueWhalePart frontBodyPart;
    public final BlueWhalePart rearBodyPart;
    public final BlueWhalePart firstTailPart;
    public final BlueWhalePart secondTailPart;
    public final BlueWhalePart tailFinPart;
    private final BlueWhalePart[] bodyParts;
    private int actionTick;
    private int lastAction = ACTION_IDLE;
    private int breatheCountdown;
    private int moistness = TOTAL_MOISTNESS;
    private int attackCooldown;
    private int stunnedTicks;
    private boolean surfacing;
    private boolean bodyPartsInitialized;
    private float previousBeachedProgress;
    private float beachedProgress;
    private final float[] yawHistory = new float[YAW_HISTORY_SIZE];
    private int yawHistoryIndex = -1;
    @Nullable
    private BlueWhalePart damagePartContext;

    public BlueWhaleEntity(EntityType<? extends BlueWhaleEntity> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 18, 10, 0.02F, 0.08F, false);
        lookControl = new SmoothSwimmingLookControl(this, 6);
        noCulling = true;
        headPart = new BlueWhalePart(this, 3.2F, 2.6F);
        frontBodyPart = new BlueWhalePart(this, 3.4F, 2.8F);
        rearBodyPart = new BlueWhalePart(this, 3.0F, 2.5F);
        firstTailPart = new BlueWhalePart(this, 2.4F, 2.0F);
        secondTailPart = new BlueWhalePart(this, 1.8F, 1.5F);
        tailFinPart = new BlueWhalePart(this, 2.7F, 0.8F);
        bodyParts = new BlueWhalePart[]{headPart, frontBodyPart, rearBodyPart, firstTailPart, secondTailPart, tailFinPart};
        breatheCountdown = random.nextInt(900, 1801);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 500.0D).add(Attributes.ATTACK_DAMAGE, 40.0D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D).add(Attributes.FOLLOW_RANGE, 48.0D).add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACTION, ACTION_IDLE);
        entityData.define(BEACHED, false);
        entityData.define(TRIDENT_COUNT, 0);
        entityData.define(STUCK_PROJECTILES, new CompoundTag());
        entityData.define(STUNNED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new SurfaceToBreatheGoal());
        goalSelector.addGoal(1, new RamAttackGoal());
        goalSelector.addGoal(4, new BottomSwimmingGoal());
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (bodyParts != null) {
            for (int i = 0; i < bodyParts.length; i++) {
                bodyParts[i].setId(id + i + 1);
            }
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public BlueWhalePart[] getParts() {
        return bodyParts;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateBeachedState();
        updateAction();
        recordYawHistory();
        updateBodyParts();
        pushEntitiesFromParts();

        if (!level().isClientSide) {
            updateMoistness();
            if (!surfacing && getTarget() == null && !isBeached() && breatheCountdown > 0) {
                // Leave the timer at 0 until SurfaceToBreatheGoal gets a chance to start on the
                // following AI tick. GoalSelector runs inside super.aiStep() before this block,
                // so resetting 0 here made canUse() miss the surface-breathing trigger forever.
                breatheCountdown--;
            }
            if (attackCooldown > 0) {
                attackCooldown--;
            }
            if (stunnedTicks > 0) {
                stunnedTicks--;
                navigation.stop();
                setDeltaMovement(Vec3.ZERO);
                setAction(ACTION_IDLE);
                if (stunnedTicks == 0) {
                    setStunned(false);
                }
            }
            if (!isStunned() && !surfacing && getTarget() == null && navigation.isDone() && getAction() == ACTION_IDLE) {
                setXRot(Mth.approachDegrees(getXRot(), 0.0F, 1.5F));
            }
            if (getAction() == ACTION_BLOW && actionTick >= 1 && actionTick <= 11) {
                emitBlowSpray();
            }
            if (getTarget() instanceof Player player && (player.isCreative() || player.isSpectator())) {
                setTarget(null);
            } else if (getTarget() != null && (!getTarget().isAlive() || distanceToSqr(getTarget()) > 4096.0D)) {
                setTarget(null);
            }
        }
    }

    /**
     * 与海豚同一套湿度机制：沾水或淋雨即回满，离水满 2400 tick 后每 tick 掉 1 点血。
     * 氧气是另一条独立的线（handleAirSupply），鲸鱼同时需要水和空气。
     */
    private void updateMoistness() {
        if (isInWaterRainOrBubble()) {
            moistness = TOTAL_MOISTNESS;
            return;
        }
        moistness--;
        if (moistness <= 0) {
            hurt(damageSources().dryOut(), DRY_OUT_DAMAGE);
        }
    }

    /**
     * 搁浅时被玩家顶动：几十吨的身体只吃水平推力，且要盖过上岸后的阻尼才推得动。
     * 传进来的方向已经是「背离推动者」，正好就是把它顶回海里的方向。
     */
    void pushFromBodyPart(double x, double z) {
        if (!isBeached() || isStunned()) {
            return;
        }
        setDeltaMovement(getDeltaMovement().add(x * BEACHED_PUSH_ASSIST, 0.0D, z * BEACHED_PUSH_ASSIST));
        hasImpulse = true;
    }

    private void updateBeachedState() {
        previousBeachedProgress = beachedProgress;
        if (onGround() && !isInWaterOrBubble()) {
            setBeached(true);
        } else if (isBeached() && isInWaterOrBubble()) {
            setBeached(false);
        }

        if (isBeached()) {
            beachedProgress = Math.min(10.0F, beachedProgress + 1.0F);
            navigation.stop();
            surfacing = false;
            setAction(ACTION_IDLE);
            setXRot(0.0F);
            setDeltaMovement(getDeltaMovement().multiply(0.45D, 1.0D, 0.45D));
            if (!level().isClientSide && tickCount % 140 == 0) {
                playSound(SoundEvents.DOLPHIN_AMBIENT, 1.1F, 0.55F);
            }
        } else {
            beachedProgress = Math.max(0.0F, beachedProgress - 1.0F);
        }
    }

    private void updateAction() {
        int action = getAction();
        if (lastAction != action) {
            lastAction = action;
            actionTick = 0;
        } else if (action != ACTION_IDLE) {
            actionTick++;
        }
        if (!level().isClientSide && ((action == ACTION_BLOW && actionTick >= BLOW_DURATION) || (action == ACTION_RAM && actionTick >= RAM_DURATION))) {
            setAction(ACTION_IDLE);
        }
    }

    private void updateBodyParts() {
        Vec3[] previousPositions = new Vec3[bodyParts.length];
        for (int i = 0; i < bodyParts.length; i++) {
            previousPositions[i] = bodyParts[i].position();
        }
        for (int i = 0; i < bodyParts.length; i++) {
            positionPart(bodyParts[i], PART_OFFSETS[i], PART_HEIGHT_OFFSETS[i]);
        }
        for (int i = 0; i < bodyParts.length; i++) {
            BlueWhalePart part = bodyParts[i];
            Vec3 previous = bodyPartsInitialized ? previousPositions[i] : part.position();
            part.xo = previous.x;
            part.yo = previous.y;
            part.zo = previous.z;
            part.xOld = previous.x;
            part.yOld = previous.y;
            part.zOld = previous.z;
        }
        bodyPartsInitialized = true;
    }

    public void refreshBodyPartsForSpawn() {
        bodyPartsInitialized = false;
        updateBodyParts();
    }

    private void positionPart(BlueWhalePart part, double distance, double yOffset) {
        Vec3 spine = spinePoint(distance);
        part.setPos(spine.x, spine.y + yOffset, spine.z);
    }

    /**
     * 求脊柱上离重心 distance 格处的世界坐标（正数在前）。
     * 身后的点逐段步进，每段取该处体节自己的历史偏航角，转弯时脊柱自然弯成 S 形而不是一根直棍。
     */
    private Vec3 spinePoint(double distance) {
        float pitch = getXRot();
        if (distance >= 0.0D) {
            // 头部领航，不吃滞后。
            return position().add(Vec3.directionFromRotation(pitch, yBodyRot).scale(distance));
        }

        Vec3 point = position();
        double remaining = -distance;
        double walked = 0.0D;
        while (remaining > 1.0E-4D) {
            double step = Math.min(SPINE_STEP, remaining);
            // 取本段中点处的偏航，避免整段用端点值导致的系统性偏移。
            Vec3 direction = Vec3.directionFromRotation(pitch, getSegmentYaw(walked + step * 0.5D, 0.0F));
            point = point.subtract(direction.scale(step));
            walked += step;
            remaining -= step;
        }
        return point;
    }

    private void recordYawHistory() {
        if (yawHistoryIndex < 0) {
            Arrays.fill(yawHistory, yBodyRot);
            yawHistoryIndex = 0;
            return;
        }
        yawHistoryIndex = (yawHistoryIndex + 1) & YAW_HISTORY_MASK;
        yawHistory[yawHistoryIndex] = yBodyRot;
    }

    /**
     * 离重心 distanceBehind 格处的体节应有的偏航角：越靠尾部读越旧的历史。
     * 渲染端传入 partialTick 做补间，否则 60fps 下会看到 20Hz 的台阶。
     */
    public float getSegmentYaw(double distanceBehind, float partialTick) {
        if (yawHistoryIndex < 0) {
            return yBodyRot;
        }
        float lag = (float) Math.max(0.0D, distanceBehind) * YAW_LAG_PER_BLOCK - partialTick;
        if (lag <= 0.0F) {
            return yBodyRot;
        }
        int whole = (int) lag;
        if (whole >= YAW_HISTORY_SIZE - 1) {
            return yawAgo(YAW_HISTORY_SIZE - 1);
        }
        float older = yawAgo(whole + 1);
        float newer = yawAgo(whole);
        return newer + Mth.wrapDegrees(older - newer) * (lag - whole);
    }

    private float yawAgo(int ticks) {
        return yawHistory[(yawHistoryIndex - ticks) & YAW_HISTORY_MASK];
    }

    private void steerToward(Vec3 target, double acceleration, float maximumPitch, float yawStep) {
        if (isStunned()) {
            return;
        }
        target = avoidObstacleAhead(target);
        Vec3 offset = target.subtract(position());
        if (offset.lengthSqr() <= 1.0E-5D) {
            return;
        }
        double horizontal = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        float targetYaw = (float) (Mth.atan2(offset.z, offset.x) * Mth.RAD_TO_DEG) - 90.0F;
        float targetPitch = Mth.clamp((float) (-Mth.atan2(offset.y, horizontal) * Mth.RAD_TO_DEG), -maximumPitch, maximumPitch);
        setYRot(Mth.approachDegrees(getYRot(), targetYaw, yawStep));
        yBodyRot = getYRot();
        yHeadRot = getYRot();
        setXRot(Mth.approachDegrees(getXRot(), targetPitch, 1.0F));

        Vec3 forward = Vec3.directionFromRotation(getXRot(), getYRot());
        setDeltaMovement(getDeltaMovement().scale(0.82D).add(forward.scale(acceleration)));
    }

    /**
     * Steers normal swimming around solid blocks in front of the whale. Ram attacks deliberately
     * bypass this so the head can break softer blocks or collide with an unbreakable obstacle.
     */
    private Vec3 avoidObstacleAhead(Vec3 target) {
        if (getAction() == ACTION_RAM || isStunned()) {
            return target;
        }

        Vec3 desired = target.subtract(position());
        if (desired.lengthSqr() < 1.0E-5D) {
            return target;
        }
        Vec3 direction = desired.normalize();
        if (clearanceDistance(direction, OBSTACLE_PROBE_DISTANCE) >= OBSTACLE_PROBE_DISTANCE) {
            return target;
        }

        Vec3 right = new Vec3(-direction.z, 0.0D, direction.x);
        if (right.lengthSqr() < 1.0E-5D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        Vec3[] candidates = new Vec3[]{
                direction.add(right.scale(1.25D)).add(0.0D, 0.12D, 0.0D).normalize(),
                direction.add(right.scale(-1.25D)).add(0.0D, 0.12D, 0.0D).normalize(),
                direction.add(0.0D, 1.15D, 0.0D).normalize(),
                direction.add(0.0D, -0.90D, 0.0D).normalize()
        };

        Vec3 bestDirection = direction;
        double bestClearance = -1.0D;
        for (Vec3 candidate : candidates) {
            double clearance = clearanceDistance(candidate, 6.0D);
            if (clearance > bestClearance) {
                bestClearance = clearance;
                bestDirection = candidate;
            }
        }

        double detourDistance = Mth.clamp(desired.length(), 4.0D, 9.0D);
        return position().add(bestDirection.scale(detourDistance));
    }

    private double clearanceDistance(Vec3 direction, double maximumDistance) {
        if (direction.lengthSqr() < 1.0E-5D) {
            return 0.0D;
        }
        Vec3 normalized = direction.normalize();
        AABB headBox = headPart.getBoundingBox().inflate(0.25D);
        for (double distance = 0.75D; distance <= maximumDistance; distance += 0.75D) {
            if (hasSolidObstacle(headBox.move(normalized.scale(distance)))) {
                return distance - 0.75D;
            }
        }
        return maximumDistance;
    }

    private boolean hasSolidObstacle(AABB box) {
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = level().getBlockState(pos);
                    if (state.isAir() || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BUBBLE_COLUMN) {
                        continue;
                    }
                    VoxelShape shape = state.getCollisionShape(level(), pos);
                    if (!shape.isEmpty() && shape.bounds().move(x, y, z).intersects(box)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Processes blocks swept by the whale's head during a ram. Soft blocks are smashed without
     * drops. Obsidian-hard (50) or unbreakable collidable blocks stop the ram and stun the whale.
     *
     * @return true when a hard obstacle stunned the whale.
     */
    private boolean processRamBlocks(Vec3 direction) {
        if (level().isClientSide || direction.lengthSqr() < 1.0E-5D) {
            return false;
        }
        Vec3 normalized = direction.normalize();
        AABB headBox = headPart.getBoundingBox().inflate(0.18D);
        double sweepDistance = Math.max(2.25D, getDeltaMovement().length() + 1.25D);
        // 每 tick 求值一次：配置开关 + mobGriefing 事件（后者让其他 mod 能按生物单独放行/拦截）。
        boolean mayBreak = FmbCommonConfig.whaleBreaksBlocks() && ForgeEventFactory.getMobGriefingEvent(level(), this);

        for (double distance = 0.35D; distance <= sweepDistance; distance += 0.35D) {
            if (processRamBlockSlice(headBox.move(normalized.scale(distance)), mayBreak)) {
                stunFromCollision();
                return true;
            }
        }
        return false;
    }

    private boolean processRamBlockSlice(AABB box, boolean mayBreak) {
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 先扫不可破坏的硬块，保证该把鲸鱼撞晕的障碍不会被它自己先拆掉。
        // 破坏被禁用时，任何带碰撞箱的方块都按硬块处理：撞墙即晕，而不是原地空转磨墙。
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = level().getBlockState(pos);
                    if (state.isAir() || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BUBBLE_COLUMN) {
                        continue;
                    }
                    float hardness = state.getDestroySpeed(level(), pos);
                    if (!mayBreak || hardness < 0.0F || hardness >= OBSIDIAN_HARDNESS) {
                        VoxelShape shape = state.getCollisionShape(level(), pos);
                        if (!shape.isEmpty() && shape.bounds().move(x, y, z).intersects(box)) {
                            return true;
                        }
                    }
                }
            }
        }

        if (!mayBreak) {
            return false;
        }

        // 比黑曜石软的方块整体拆除，海带/海草这类无碰撞装饰一并清掉。
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = level().getBlockState(pos);
                    if (state.isAir() || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BUBBLE_COLUMN) {
                        continue;
                    }
                    float hardness = state.getDestroySpeed(level(), pos);
                    if (hardness >= 0.0F && hardness < OBSIDIAN_HARDNESS
                            && new AABB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).intersects(box)) {
                        BlockPos target = pos.immutable();
                        // 领地/保护类 mod 的拦截入口；被拒绝的实心方块同样把鲸鱼撞晕。
                        if (!ForgeEventFactory.onEntityDestroyBlock(this, target, state)) {
                            if (!state.getCollisionShape(level(), target).isEmpty()) {
                                return true;
                            }
                            continue;
                        }
                        level().destroyBlock(target, false);
                    }
                }
            }
        }
        return false;
    }

    private void stunFromCollision() {
        stunnedTicks = STUN_DURATION;
        setStunned(true);
        navigation.stop();
        surfacing = false;
        setAction(ACTION_IDLE);
        setDeltaMovement(Vec3.ZERO);
        attackCooldown = Math.max(attackCooldown, STUN_DURATION);
    }

    private void pushEntitiesFromParts() {
        for (BlueWhalePart part : bodyParts) {
            for (Entity entity : level().getEntities(part, part.getBoundingBox().inflate(0.1D), candidate -> candidate.isPushable() && candidate != this && !(candidate instanceof BlueWhalePart))) {
                entity.push(this);
            }
        }
    }

    private void emitBlowSpray() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 spout = getSpoutPosition();
        double rise = actionTick * 0.22D;
        serverLevel.sendParticles(ParticleTypes.CLOUD, spout.x, spout.y + rise, spout.z, 12, 0.3D, 0.35D, 0.3D, 0.06D);
        if (actionTick <= 4) {
            serverLevel.sendParticles(ParticleTypes.POOF, spout.x, spout.y + rise * 0.5D, spout.z, 5, 0.18D, 0.3D, 0.18D, 0.03D);
        }
        AABB spray = new AABB(spout.x - 1.25D, spout.y, spout.z - 1.25D, spout.x + 1.25D, spout.y + 4.2D, spout.z + 1.25D);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, spray, player -> !player.isSpectator())) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
        }
    }

    public Vec3 getSpoutPosition() {
        return new Vec3(headPart.getX(), headPart.getY() + headPart.getBbHeight() + 0.15D, headPart.getZ());
    }

    public int getAction() {
        return entityData.get(ACTION);
    }

    public int getActionTick() {
        return actionTick;
    }

    public void setAction(int action) {
        if (entityData.get(ACTION) != action) {
            entityData.set(ACTION, action);
            lastAction = action;
            actionTick = 0;
        }
    }

    public boolean isBeached() {
        return entityData.get(BEACHED);
    }

    public void setBeached(boolean beached) {
        entityData.set(BEACHED, beached);
    }

    public boolean isStunned() {
        return entityData.get(STUNNED);
    }

    private void setStunned(boolean stunned) {
        entityData.set(STUNNED, stunned);
    }

    public float getBeachedProgress(float partialTick) {
        return Mth.lerp(partialTick, previousBeachedProgress, beachedProgress) / 10.0F;
    }

    public int getTridentCount() {
        return entityData.get(TRIDENT_COUNT);
    }

    public ListTag getStuckProjectileData() {
        return entityData.get(STUCK_PROJECTILES).getList("Entries", Tag.TAG_COMPOUND);
    }

    boolean hurtFromPart(BlueWhalePart part, DamageSource source, float amount) {
        BlueWhalePart previous = damagePartContext;
        damagePartContext = part;
        try {
            return hurt(source, amount);
        } finally {
            damagePartContext = previous;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this || source.getEntity() instanceof BlueWhalePart) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        // 投射物不看这一下有没有真掉血：无敌帧内命中的三叉戟同样要插住，否则会掉回地上。
        // 但已经死掉的鲸鱼不再接收，那时战利品已经掉完，收下等于凭空吞掉玩家的三叉戟。
        if (!level().isClientSide && isAlive() && source.getDirectEntity() instanceof AbstractArrow arrow) {
            boolean trident = arrow instanceof ThrownTrident;
            // 三叉戟连附魔一起收进鲸鱼身上，拔下来或鲸鱼死亡时才还回世界。
            ItemStack stuckItem = trident ? ((ThrownTridentAccessor) arrow).callGetPickupItem() : ItemStack.EMPTY;
            boolean stored = recordStuckProjectile(arrow, damagePartContext, trident, stuckItem);
            if (trident && stored) {
                arrow.discard();
            }
        }
        if (hurt && !level().isClientSide
                && source.getEntity() instanceof LivingEntity attacker
                && !(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            setTarget(attacker);
        }
        return hurt;
    }

    private boolean recordStuckProjectile(AbstractArrow projectile, @Nullable BlueWhalePart hitPart, boolean trident, ItemStack stuckItem) {
        BlueWhalePart part = hitPart != null ? hitPart : findClosestBodyPart(projectile.position());
        if (part == null) {
            return false;
        }

        Vec3 velocity = projectile.getDeltaMovement();
        Vec3 from = projectile.position().subtract(velocity);
        Vec3 to = projectile.position().add(velocity);
        AABB hitBox = part.getBoundingBox().inflate(0.05D);
        Optional<Vec3> clipped = hitBox.clip(from, to);
        Vec3 hit = clipped.orElseGet(() -> closestPoint(hitBox, projectile.position()));

        Vec3[] axes = bodyAxes();
        Vec3 right = axes[0];
        Vec3 up = axes[1];
        Vec3 forward = axes[2];
        Vec3 center = part.getBoundingBox().getCenter();
        Vec3 offset = hit.subtract(center);
        double halfWidth = Math.max(0.001D, part.getBbWidth() * 0.5D);
        double halfHeight = Math.max(0.001D, part.getBbHeight() * 0.5D);

        Vec3 direction = velocity.lengthSqr() > 1.0E-6D ? velocity.normalize() : forward.scale(-1.0D);
        CompoundTag entry = new CompoundTag();
        entry.putBoolean("Trident", trident);
        entry.putByte("Part", (byte) getPartIndex(part));
        entry.putFloat("X", (float) Mth.clamp(offset.dot(right) / halfWidth, -1.0D, 1.0D));
        entry.putFloat("Y", (float) Mth.clamp(offset.dot(up) / halfHeight, -1.0D, 1.0D));
        entry.putFloat("Z", (float) Mth.clamp(offset.dot(forward) / halfWidth, -1.0D, 1.0D));
        entry.putFloat("DX", (float) direction.dot(right));
        entry.putFloat("DY", (float) direction.dot(up));
        entry.putFloat("DZ", (float) direction.dot(forward));
        if (!stuckItem.isEmpty()) {
            entry.put("Item", stuckItem.save(new CompoundTag()));
        }

        CompoundTag data = entityData.get(STUCK_PROJECTILES).copy();
        ListTag oldEntries = data.getList("Entries", Tag.TAG_COMPOUND);
        ListTag entries = new ListTag();
        int sameTypeKept = 0;
        int limit = trident ? MAX_STUCK_TRIDENTS - 1 : MAX_STUCK_ARROWS - 1;
        for (int i = oldEntries.size() - 1; i >= 0; i--) {
            CompoundTag old = oldEntries.getCompound(i);
            if (old.getBoolean("Trident") == trident) {
                if (sameTypeKept++ >= limit) {
                    // 挤掉最老的一根：带物品的必须还回世界，不能凭空吞掉玩家的三叉戟。
                    dropStuckItem(old);
                    continue;
                }
            }
            entries.add(0, old.copy());
        }
        entries.add(entry);
        data.put("Entries", entries);
        entityData.set(STUCK_PROJECTILES, data);
        refreshTridentCount(entries);
        return true;
    }

    /** 机体坐标系：右 / 上 / 前，插着的投射物按这三轴归一化记录。 */
    private Vec3[] bodyAxes() {
        Vec3 forward = Vec3.directionFromRotation(getXRot(), yBodyRot).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
        return new Vec3[]{right, right.cross(forward).normalize(), forward};
    }

    /** 由记录的归一化坐标还原插着的世界位置，与 recordStuckProjectile 互为逆运算。 */
    private Vec3 stuckProjectilePosition(CompoundTag entry) {
        BlueWhalePart part = bodyParts[Mth.clamp(entry.getByte("Part"), 0, bodyParts.length - 1)];
        Vec3[] axes = bodyAxes();
        double halfWidth = Math.max(0.001D, part.getBbWidth() * 0.5D);
        double halfHeight = Math.max(0.001D, part.getBbHeight() * 0.5D);
        return part.getBoundingBox().getCenter()
                .add(axes[0].scale(entry.getFloat("X") * halfWidth))
                .add(axes[1].scale(entry.getFloat("Y") * halfHeight))
                .add(axes[2].scale(entry.getFloat("Z") * halfWidth));
    }

    private void refreshTridentCount(ListTag entries) {
        int count = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.getCompound(i).getBoolean("Trident")) {
                count++;
            }
        }
        entityData.set(TRIDENT_COUNT, Math.min(MAX_STUCK_TRIDENTS, count));
    }

    private void dropStuckItem(CompoundTag entry) {
        if (level().isClientSide || !entry.contains("Item", Tag.TAG_COMPOUND)) {
            return;
        }
        ItemStack stack = ItemStack.of(entry.getCompound("Item"));
        if (stack.isEmpty()) {
            return;
        }
        Vec3 position = stuckProjectilePosition(entry);
        ItemEntity item = new ItemEntity(level(), position.x, position.y, position.z, stack);
        item.setDefaultPickUpDelay();
        level().addFreshEntity(item);
    }

    /** 死亡时把插着的三叉戟原样归还世界。 */
    private void dropStuckTridents() {
        CompoundTag data = entityData.get(STUCK_PROJECTILES).copy();
        ListTag entries = data.getList("Entries", Tag.TAG_COMPOUND);
        ListTag remaining = new ListTag();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.contains("Item", Tag.TAG_COMPOUND)) {
                dropStuckItem(entry);
            } else {
                remaining.add(entry.copy());
            }
        }
        data.put("Entries", remaining);
        entityData.set(STUCK_PROJECTILES, data);
        refreshTridentCount(remaining);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).isEmpty()) {
            InteractionResult result = pullStuckTrident(player);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return super.mobInteract(player, hand);
    }

    /** 空手右键拔三叉戟：取视线最对准的一根，谁拔谁立刻被记恨。 */
    private InteractionResult pullStuckTrident(Player player) {
        CompoundTag data = entityData.get(STUCK_PROJECTILES).copy();
        ListTag entries = data.getList("Entries", Tag.TAG_COMPOUND);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        int best = -1;
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (!entry.contains("Item", Tag.TAG_COMPOUND)) {
                continue;
            }
            Vec3 offset = stuckProjectilePosition(entry).subtract(eye);
            double distance = offset.length();
            if (distance > PULL_REACH) {
                continue;
            }
            // 视线越对准、距离越近越优先，避免在庞大的身体上拔错一根。
            double aim = distance < 1.0E-4D ? 0.0D : 1.0D - offset.scale(1.0D / distance).dot(look);
            double score = aim * 4.0D + distance;
            if (score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        if (best < 0) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        CompoundTag entry = entries.getCompound(best);
        ItemStack stack = ItemStack.of(entry.getCompound("Item"));
        entries.remove(best);
        data.put("Entries", entries);
        entityData.set(STUCK_PROJECTILES, data);
        refreshTridentCount(entries);

        if (!stack.isEmpty() && !player.addItem(stack)) {
            player.drop(stack, false);
        }
        playSound(SoundEvents.TRIDENT_RETURN, 1.2F, 0.85F + random.nextFloat() * 0.2F);
        if (!player.isCreative() && !player.isSpectator()) {
            setLastHurtByMob(player);
            setTarget(player);
        }
        return InteractionResult.CONSUME;
    }

    private int getPartIndex(BlueWhalePart part) {
        for (int i = 0; i < bodyParts.length; i++) {
            if (bodyParts[i] == part) {
                return i;
            }
        }
        return 1;
    }

    @Nullable
    private BlueWhalePart findClosestBodyPart(Vec3 point) {
        BlueWhalePart closest = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlueWhalePart part : bodyParts) {
            Vec3 nearest = closestPoint(part.getBoundingBox(), point);
            double distance = nearest.distanceToSqr(point);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = part;
            }
        }
        return closest;
    }

    private static Vec3 closestPoint(AABB box, Vec3 point) {
        return new Vec3(
                Mth.clamp(point.x, box.minX, box.maxX),
                Mth.clamp(point.y, box.minY, box.maxY),
                Mth.clamp(point.z, box.minZ, box.maxZ)
        );
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        dropStuckTridents();
        scatterAlongBody(butcherDrops(looting));
    }

    /** 按配置切出战利品：默认鲸鱼肉，另一档是原版鳕鱼加骨粉。 */
    private List<ItemStack> butcherDrops(int looting) {
        int bonus = Math.max(0, looting);
        List<ItemStack> drops = new ArrayList<>();
        if (FmbCommonConfig.whaleDrop() == FmbCommonConfig.WhaleDrop.COD_AND_BONE_MEAL) {
            int cod = 8 + random.nextInt(7) + bonus;
            for (int i = 0; i < cod; i++) {
                drops.add(new ItemStack(Items.COD));
            }
            int boneMeal = 4 + random.nextInt(5) + bonus;
            for (int i = 0; i < boneMeal; i++) {
                drops.add(new ItemStack(Items.BONE_MEAL));
            }
        } else {
            int steak = 8 + random.nextInt(7) + bonus;
            for (int i = 0; i < steak; i++) {
                drops.add(new ItemStack(FishingMadeBetterForge.registeredItem("whale_steak")));
            }
        }
        return drops;
    }

    /** 沿体轴一路铺开，而不是全堆在死亡点上：这么大一头鲸鱼，掉落也该有那个体量感。 */
    private void scatterAlongBody(List<ItemStack> drops) {
        int count = drops.size();
        if (count <= 0) {
            return;
        }
        Vec3 forward = Vec3.directionFromRotation(0.0F, yBodyRot).normalize();
        Vec3 sideways = new Vec3(-forward.z, 0.0D, forward.x);
        for (int i = 0; i < count; i++) {
            double along = count == 1 ? 0.0D : -3.4D + 6.8D * i / (count - 1.0D);
            double side = ((i & 1) == 0 ? -0.45D : 0.45D) + (random.nextDouble() - 0.5D) * 0.2D;
            Vec3 dropPosition = position().add(forward.scale(along)).add(sideways.scale(side)).add(0.0D, 0.6D + (i % 3) * 0.18D, 0.0D);
            ItemEntity item = new ItemEntity(level(), dropPosition.x, dropPosition.y, dropPosition.z, drops.get(i));
            item.setDefaultPickUpDelay();
            item.setDeltaMovement(sideways.scale(side * 0.08D).add(0.0D, 0.12D, 0.0D));
            level().addFreshEntity(item);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Beached", isBeached());
        tag.putInt("BreatheCountdown", breatheCountdown);
        tag.putInt("Moistness", moistness);
        tag.putInt("EmbeddedTridents", getTridentCount());
        tag.putInt("StunnedTicks", stunnedTicks);
        tag.put("StuckProjectiles", entityData.get(STUCK_PROJECTILES).copy());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBeached(tag.getBoolean("Beached"));
        breatheCountdown = tag.contains("BreatheCountdown") ? tag.getInt("BreatheCountdown") : random.nextInt(900, 1801);
        moistness = tag.contains("Moistness") ? tag.getInt("Moistness") : TOTAL_MOISTNESS;
        stunnedTicks = Math.max(0, tag.getInt("StunnedTicks"));
        setStunned(stunnedTicks > 0);
        entityData.set(STUCK_PROJECTILES, tag.contains("StuckProjectiles", Tag.TAG_COMPOUND)
                ? tag.getCompound("StuckProjectiles").copy()
                : new CompoundTag());
        // 数量以条目为准，避免存档里的计数与实际插着的投射物对不上。
        refreshTridentCount(getStuckProjectileData());
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    @Override
    protected void handleAirSupply(int airSupply) {
        if (isEyeInFluid(FluidTags.WATER)) {
            // Whales breathe air. Consume one air point every 100 ticks instead of every tick.
            if (tickCount % AIR_CONSUMPTION_INTERVAL == 0) {
                int nextAir = airSupply - 1;
                setAirSupply(nextAir);
                if (nextAir <= -20) {
                    setAirSupply(0);
                    hurt(damageSources().drown(), 2.0F);
                }
            }
        } else {
            setAirSupply(increaseAirSupply(airSupply));
        }
    }

    @Override
    public int getMaxAirSupply() {
        return 4000;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return getMaxAirSupply();
    }

    @Override
    public boolean isPushedByFluid() {
        return isBeached();
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.86F;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isStunned()) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (isEffectiveAi() && isInWater()) {
            moveRelative(getSpeed(), travelVector);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(isBeached() ? 0.5D : 0.9D));
            if (getTarget() == null && !surfacing) {
                setDeltaMovement(getDeltaMovement().add(0.0D, -0.0025D, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.DOLPHIN_AMBIENT_WATER;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }

    private final class SurfaceToBreatheGoal extends Goal {
        private BlockPos surfaceAir;
        private boolean blowStarted;
        private boolean emergencyBreathing;

        private SurfaceToBreatheGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (isBeached() || isStunned() || !isInWaterOrBubble()) {
                return false;
            }
            emergencyBreathing = getAirSupply() < 600;
            if (!emergencyBreathing && (getTarget() != null || getAction() == ACTION_RAM)) {
                return false;
            }
            if (!emergencyBreathing && breatheCountdown > 0) {
                return false;
            }
            surfaceAir = findSurfaceAir();
            return surfaceAir != null;
        }

        @Override
        public boolean canContinueToUse() {
            return surfacing && !isBeached() && !isStunned() && surfaceAir != null
                    && (getTarget() == null || emergencyBreathing)
                    && (!blowStarted || getAction() == ACTION_BLOW);
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            surfacing = true;
            blowStarted = false;
            navigation.stop();
        }

        @Override
        public void tick() {
            if (blowStarted) {
                Vec3 movement = getDeltaMovement();
                Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot()).scale(0.006D);
                setDeltaMovement(movement.x * 0.88D + forward.x, Math.min(movement.y, 0.0D), movement.z * 0.88D + forward.z);
                setXRot(Mth.approachDegrees(getXRot(), 0.0F, 1.5F));
                return;
            }
            double targetY = surfaceAir.getY() - getBbHeight() + 0.35D;
            steerToward(new Vec3(surfaceAir.getX() + 0.5D, targetY, surfaceAir.getZ() + 0.5D), 0.018D, 18.0F, 2.0F);
            if (!isEyeInFluid(FluidTags.WATER)) {
                blowStarted = true;
                setAirSupply(getMaxAirSupply());
                setAction(ACTION_BLOW);
                playSound(SoundEvents.PLAYER_BREATH, 1.6F, 0.55F);
            }
        }

        @Override
        public void stop() {
            surfacing = false;
            surfaceAir = null;
            blowStarted = false;
            emergencyBreathing = false;
            breatheCountdown = random.nextInt(900, 1801);
            if (getAction() == ACTION_BLOW) {
                setAction(ACTION_IDLE);
            }
        }

        @Nullable
        private BlockPos findSurfaceAir() {
            // First try the whale's own water column. The old implementation only sampled
            // random points in front of it, so it could repeatedly fail to find the surface
            // even with open air directly overhead.
            BlockPos direct = findSurfaceAirAt(getBlockX(), getBlockZ());
            if (direct != null) {
                return direct;
            }

            Vec3 forward = getLookAngle();
            for (int attempt = 0; attempt < 16; attempt++) {
                int distance = 4 + random.nextInt(13);
                int x = Mth.floor(getX() + forward.x * distance + random.nextInt(-7, 8));
                int z = Mth.floor(getZ() + forward.z * distance + random.nextInt(-7, 8));
                BlockPos air = findSurfaceAirAt(x, z);
                if (air != null) {
                    return air;
                }
            }
            return null;
        }

        @Nullable
        private BlockPos findSurfaceAirAt(int x, int z) {
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, Mth.floor(getEyeY()), z);
            if (!level().getFluidState(cursor).is(FluidTags.WATER)) {
                cursor.setY(Mth.floor(getY()));
            }
            if (!level().getFluidState(cursor).is(FluidTags.WATER)) {
                return null;
            }

            while (cursor.getY() < level().getMaxBuildHeight() - 1 && level().getFluidState(cursor).is(FluidTags.WATER)) {
                cursor.move(0, 1, 0);
            }

            BlockPos air = cursor.immutable();
            return level().getFluidState(air.below()).is(FluidTags.WATER) && level().getBlockState(air).isAir() ? air : null;
        }
    }

    private final class BottomSwimmingGoal extends Goal {
        private int travelTicks;
        private BlockPos swimTarget;

        private BottomSwimmingGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (isBeached() || isStunned() || surfacing || getTarget() != null || !isInWaterOrBubble() || random.nextInt(70) != 0) {
                return false;
            }
            swimTarget = findBottomWater();
            return swimTarget != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !isBeached() && !isStunned() && !surfacing && getTarget() == null && swimTarget != null
                    && Vec3.atCenterOf(swimTarget).distanceToSqr(position()) > 6.25D && travelTicks < 420;
        }

        @Override
        public void start() {
            travelTicks = 0;
        }

        @Override
        public void tick() {
            travelTicks++;
            steerToward(Vec3.atCenterOf(swimTarget), 0.014D, 12.0F, 2.5F);
        }

        @Override
        public void stop() {
            swimTarget = null;
        }

        @Nullable
        private BlockPos findBottomWater() {
            for (int attempt = 0; attempt < 14; attempt++) {
                int x = getBlockX() + random.nextInt(-20, 21);
                int z = getBlockZ() + random.nextInt(-20, 21);
                int startY = Math.min(level().getSeaLevel() + 2, getBlockY() + 10);
                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, startY, z);
                while (cursor.getY() > level().getMinBuildHeight() + 2 && !level().getFluidState(cursor).is(FluidTags.WATER)) {
                    cursor.move(0, -1, 0);
                }
                if (!level().getFluidState(cursor).is(FluidTags.WATER)) {
                    continue;
                }
                while (cursor.getY() > level().getMinBuildHeight() + 2 && level().getFluidState(cursor.below()).is(FluidTags.WATER)) {
                    cursor.move(0, -1, 0);
                }
                BlockPos target = cursor.above(2 + random.nextInt(3)).immutable();
                if (level().getFluidState(target).is(FluidTags.WATER) && level().getFluidState(target.above()).is(FluidTags.WATER)) {
                    return target;
                }
            }
            return null;
        }
    }

    private final class RamAttackGoal extends Goal {
        private static final int WINDUP = 0;
        private static final int CHARGE = 1;
        private static final int RECOVERY = 2;
        private static final int DONE = 3;
        private int phase;
        private int phaseTicks;
        private boolean secondRam;

        private RamAttackGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return attackCooldown <= 0 && target != null && target.isAlive() && !isBeached() && !isStunned() && isInWaterOrBubble();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = getTarget();
            return phase != DONE && target != null && target.isAlive() && !isBeached() && !isStunned() && isInWaterOrBubble();
        }

        @Override
        public void start() {
            secondRam = false;
            beginWindup(12);
        }

        private void beginWindup(int ticks) {
            phase = WINDUP;
            phaseTicks = ticks;
            navigation.stop();
            setAction(ACTION_IDLE);
        }

        private void beginCharge() {
            phase = CHARGE;
            phaseTicks = RAM_DURATION;
            setAction(ACTION_RAM);
        }

        private void beginRecovery() {
            phase = RECOVERY;
            phaseTicks = 30;
            navigation.stop();
            setAction(ACTION_IDLE);
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) {
                phase = DONE;
                return;
            }
            getLookControl().setLookAt(target, 30.0F, 20.0F);
            if (phase == WINDUP) {
                navigation.stop();
                setDeltaMovement(getDeltaMovement().scale(0.82D));
                if (--phaseTicks <= 0) {
                    beginCharge();
                }
                return;
            }
            if (phase == CHARGE) {
                Vec3 direction = target.getEyePosition().subtract(getEyePosition());
                if (direction.lengthSqr() > 1.0E-5D) {
                    direction = direction.normalize();
                    if (processRamBlocks(direction)) {
                        phase = DONE;
                        return;
                    }

                    // Multipart placement follows body yaw/pitch rather than LookControl.
                    // Keep the whale's actual head/body axis aimed at the target throughout the ram.
                    double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
                    float targetYaw = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F;
                    float targetPitch = Mth.clamp((float) (-Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG), -30.0F, 30.0F);
                    setYRot(Mth.approachDegrees(getYRot(), targetYaw, 12.0F));
                    yBodyRot = getYRot();
                    yHeadRot = getYRot();
                    setXRot(Mth.approachDegrees(getXRot(), targetPitch, 6.0F));

                    setDeltaMovement(getDeltaMovement().scale(0.68D).add(direction.scale(0.22D)));
                    getMoveControl().setWantedPosition(target.getX(), target.getEyeY(), target.getZ(), 1.55D);
                }
                if (headPart.getBoundingBox().inflate(0.7D).intersects(target.getBoundingBox())) {
                    target.hurt(damageSources().mobAttack(BlueWhaleEntity.this),
                            (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
                    target.push(direction.x * 2.2D, 0.45D, direction.z * 2.2D);
                    beginRecovery();
                } else if (--phaseTicks <= 0) {
                    beginRecovery();
                }
                return;
            }
            if (phase == RECOVERY) {
                setDeltaMovement(getDeltaMovement().scale(0.84D));
                if (--phaseTicks <= 0) {
                    if (!secondRam && random.nextFloat() < 0.4F) {
                        secondRam = true;
                        beginWindup(9);
                    } else {
                        attackCooldown = 80;
                        phase = DONE;
                    }
                }
            }
        }

        @Override
        public void stop() {
            navigation.stop();
            setAction(ACTION_IDLE);
            phase = DONE;
        }
    }
}
