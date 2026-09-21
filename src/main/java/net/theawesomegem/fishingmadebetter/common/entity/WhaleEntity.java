package net.theawesomegem.fishingmadebetter.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.theawesomegem.fishingmadebetter.common.config.FmbCommonConfig;
import net.theawesomegem.fishingmadebetter.registry.ModSounds;

import javax.annotation.Nullable;
import java.util.*;

public final class WhaleEntity extends WaterAnimal {
    public static final int ACTION_IDLE = 0;
    public static final int ACTION_BLOW = 1;
    public static final int ACTION_RAM = 2;
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(WhaleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BEACHED = SynchedEntityData.defineId(WhaleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED = SynchedEntityData.defineId(WhaleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int BLOW_DURATION = 32;
    private static final int RAM_DURATION = 24;
    private static final double RAM_KNOCKBACK_HORIZONTAL = 4.40D;
    private static final double RAM_KNOCKBACK_VERTICAL = 0.90D;
    private static final int AIR_CONSUMPTION_INTERVAL = 100;
    private static final int STUN_DURATION = 60;
    private static final int SHIELD_DISABLE_DURATION = 200;
    private static final int TOTAL_MOISTNESS = 2400;
    private static final float DRY_OUT_DAMAGE = 1.0F;
    private static final double BEACHED_PUSH_ASSIST = 2.0D;
    private static final int MAX_STUCK_ARROWS = 16;
    private static final float OBSIDIAN_HARDNESS = 50.0F;
    private static final double OBSTACLE_PROBE_DISTANCE = 9.0D;
    private static final int YAW_HISTORY_SIZE = 32;
    private static final int YAW_HISTORY_MASK = YAW_HISTORY_SIZE - 1;
    private static final float YAW_LAG_PER_BLOCK = 1.1F;
    private static final double SPINE_STEP = 0.5D;
    // Model-space longitudinal centers (blocks). Positive is toward the head.
    // These cover the actual 512x512 large Blockbench model from the snout through the fluke.
    private static final double[] PART_OFFSETS = {1.85D, -0.94D, -3.56D, -5.75D, -8.10D, -10.80D};
    // Vertical center of each multipart relative to the main body's center. These values come
    // directly from the large Blockbench model's neutral-pose cube bounds after moving Controller
    // from Y=24 to Y=0. Keeping centers instead of common bottom offsets lets the boxes follow
    // pitch without floating above the mesh.
    private static final double[] PART_CENTER_UP_OFFSETS = {0.13D, 0.0D, 0.0D, 0.13D, 0.81D, 1.06D};
    private static final double BODY_CENTER_HEIGHT = 1.875D;
    private static final int SPINE_PART_COUNT = 6;
    private static final double FIN_LONGITUDINAL_OFFSET = -2.23D;
    private static final double LEFT_FIN_LATERAL_OFFSET = -3.125D;
    private static final double RIGHT_FIN_LATERAL_OFFSET = 2.875D;
    private static final double FIN_CENTER_UP_OFFSET = 0.25D;

    public final WhalePart headPart;
    public final WhalePart frontBodyPart;
    public final WhalePart rearBodyPart;
    public final WhalePart firstTailPart;
    public final WhalePart secondTailPart;
    public final WhalePart tailFinPart;
    public final WhalePart leftFinPart;
    public final WhalePart rightFinPart;
    private final WhalePart[] bodyParts;
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
    private float previousVisualPitch;
    private float visualPitch;
    private float yawVelocity;
    private final float[] yawHistory = new float[YAW_HISTORY_SIZE];
    private int yawHistoryIndex = -1;
    private final List<ItemStack> capturedWhaleLoot = new ArrayList<>();
    private boolean capturingWhaleLoot;

    public final AnimationState beachedAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState ramAnimationState = new AnimationState();
    public final AnimationState blowAnimationState = new AnimationState();

    @Nullable
    private WhalePart damagePartContext;

    public WhaleEntity(EntityType<? extends WhaleEntity> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 18, 10, 0.02F, 0.08F, false);
        lookControl = new SmoothSwimmingLookControl(this, 6);
        noCulling = true;
        headPart = new WhalePart(this, 4.25F, 4.45F);
        frontBodyPart = new WhalePart(this, 3.60F, 3.75F);
        rearBodyPart = new WhalePart(this, 2.80F, 3.50F);
        firstTailPart = new WhalePart(this, 2.30F, 2.80F);
        secondTailPart = new WhalePart(this, 1.55F, 1.40F);
        tailFinPart = new WhalePart(this, 3.55F, 0.90F);
        // The large model's pectoral fins extend about 2.5 blocks from their local center.
        leftFinPart = new WhalePart(this, 2.55F, 0.50F);
        rightFinPart = new WhalePart(this, 2.55F, 0.50F);
        bodyParts = new WhalePart[]{headPart, frontBodyPart, rearBodyPart, firstTailPart, secondTailPart, tailFinPart, leftFinPart, rightFinPart};
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
    public WhalePart[] getParts() {
        return bodyParts;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateBeachedState();
        updateVisualPitch();
        updateAction();
        updateAnimationStates();
        updateSmoothBodyRotation();
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
     * Keeps the rendered body on a broad turning arc instead of snapping it to the navigation
     * heading. Multipart placement uses the same body yaw, so the visible whale and its hitboxes
     * remain together while turning.
     */
    private void updateSmoothBodyRotation() {
        if (yawHistoryIndex < 0) {
            yBodyRot = getYRot();
            yHeadRot = getYRot();
            return;
        }
        float bodyStep = getAction() == ACTION_RAM ? 3.5F : 2.0F;
        float headStep = getAction() == ACTION_RAM ? 4.5F : 3.0F;
        yBodyRot = Mth.approachDegrees(yBodyRot, getYRot(), bodyStep);
        yHeadRot = Mth.approachDegrees(yHeadRot, getYRot(), headStep);
    }

    /**
     * Prevent Mob's BodyRotationControl from replacing the whale's smoothed body rotation.
     */
    @Override
    protected float tickHeadTurn(float movementYaw, float animationStep) {
        return animationStep;
    }

    private void updateVisualPitch() {
        previousVisualPitch = visualPitch;
        // Use only the whale's intentional pitch. Deriving render pitch from tiny changes in water
        // velocity made this very long model rock up/down around its origin while barely moving.
        float targetPitch = isBeached() ? 0.0F : getXRot();
        visualPitch = Mth.approachDegrees(visualPitch, targetPitch, isStunned() ? 0.8F : 1.6F);
    }

    public float getVisualPitch(float partialTick) {
        return Mth.lerp(Mth.clamp(partialTick, 0.0F, 1.0F), previousVisualPitch, visualPitch);
    }

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

    @Override
    public boolean isPushable() {
        return isBeached();
    }

    @Override
    public void push(Entity entity) {
        if (isBeached()) {
            super.push(entity);
        }
    }

    @Override
    public void push(double x, double y, double z) {
        if (isBeached()) {
            super.push(x, y, z);
        }
    }

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
                playSound(ModSounds.WHALE_AMBIENT.get(), 1.1F, 0.72F);
            }
        } else {
            beachedProgress = Math.max(0.0F, beachedProgress - 1.0F);
        }
    }

    private void updateAnimationStates() {
        if (!level().isClientSide) {
            return;
        }

        boolean stunned = isStunned();
        boolean beached = isBeached() && !stunned;
        int action = getAction();
        Vec3 movement = getDeltaMovement();
        boolean moving = movement.horizontalDistanceSqr() > 4.0E-4D || Math.abs(movement.y) > 0.02D;
        beachedAnimationState.animateWhen(beached, tickCount);
        swimAnimationState.animateWhen(!beached && !stunned && action == ACTION_IDLE && isInWaterOrBubble() && moving, tickCount);
        ramAnimationState.animateWhen(!beached && !stunned && action == ACTION_RAM, tickCount);
        blowAnimationState.animateWhen(!beached && !stunned && action == ACTION_BLOW, tickCount);
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
        for (int i = 0; i < SPINE_PART_COUNT; i++) {
            positionPart(bodyParts[i], PART_OFFSETS[i], PART_CENTER_UP_OFFSETS[i]);
        }
        positionFinPart(leftFinPart, LEFT_FIN_LATERAL_OFFSET);
        positionFinPart(rightFinPart, RIGHT_FIN_LATERAL_OFFSET);
        for (int i = 0; i < bodyParts.length; i++) {
            WhalePart part = bodyParts[i];
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

    private void positionPart(WhalePart part, double distance, double centerUpOffset) {
        Vec3[] axes = bodyAxes();
        Vec3 center = spinePoint(distance).add(axes[1].scale(centerUpOffset));
        part.setPos(center.x, center.y - part.getBbHeight() * 0.5D, center.z);
    }

    private void positionFinPart(WhalePart part, double lateralOffset) {
        Vec3[] axes = bodyAxes();
        Vec3 center = spinePoint(FIN_LONGITUDINAL_OFFSET)
                .add(axes[0].scale(lateralOffset))
                .add(axes[1].scale(FIN_CENTER_UP_OFFSET));
        part.setPos(center.x, center.y - part.getBbHeight() * 0.5D, center.z);
    }

    /**
     * 求脊柱上离重心 distance 格处的世界坐标（正数在前）。
     * 身后的点逐段步进，每段取该处体节自己的历史偏航角，转弯时脊柱自然弯成 S 形而不是一根直棍。
     */
    private Vec3 spinePoint(double distance) {
        float pitch = getXRot();
        Vec3 point = position().add(0.0D, BODY_CENTER_HEIGHT, 0.0D);
        if (distance >= 0.0D) {
            // 头部领航，不吃滞后。基准点放在主体中心而不是实体脚底，俯仰时 multipart
            // 才会围绕与模型相同的轴转动。
            return point.add(Vec3.directionFromRotation(pitch, yBodyRot).scale(distance));
        }
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
        // The renderer interpolates from the previous tick to the current tick. Including that
        // one-tick window here prevents the first tail joint from stepping against the body.
        float lag = (float) Math.max(0.0D, distanceBehind) * YAW_LAG_PER_BLOCK + 1.0F - Mth.clamp(partialTick, 0.0F, 1.0F);
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
        turnToward(targetYaw, yawStep, Math.max(0.12F, yawStep * 0.12F));
        setXRot(Mth.approachDegrees(getXRot(), targetPitch, 1.0F));

        Vec3 forward = Vec3.directionFromRotation(getXRot(), yBodyRot);
        setDeltaMovement(getDeltaMovement().scale(0.82D).add(forward.scale(acceleration)));
    }

    private void turnToward(float targetYaw, float maximumSpeed, float acceleration) {
        float error = Mth.wrapDegrees(targetYaw - getYRot());
        float desiredVelocity = Mth.clamp(error * 0.18F, -maximumSpeed, maximumSpeed);
        yawVelocity = Mth.approach(yawVelocity, desiredVelocity, acceleration);
        if (Math.abs(error) < Math.abs(yawVelocity)) {
            yawVelocity = error;
        }
        setYRot(getYRot() + yawVelocity);
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

        Vec3[] candidates = new Vec3[]{direction.add(right.scale(1.25D)).add(0.0D, 0.12D, 0.0D).normalize(), direction.add(right.scale(-1.25D)).add(0.0D, 0.12D, 0.0D).normalize(), direction.add(0.0D, 1.15D, 0.0D).normalize(), direction.add(0.0D, -0.90D, 0.0D).normalize()};

        Vec3 bestDirection = direction;
        double bestClearance = -1.0D;
        for (Vec3 candidate : candidates) {
            double clearance = clearanceDistance(candidate, 12.0D);
            if (clearance > bestClearance) {
                bestClearance = clearance;
                bestDirection = candidate;
            }
        }

        double detourDistance = Mth.clamp(desired.length(), 8.0D, 18.0D);
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

    private static boolean isBreathingIce(BlockState state) {
        return state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE);
    }

    /**
     * While the whale is deliberately surfacing to breathe, clear a head-sized opening through
     * vanilla ice, packed ice and blue ice.  This is independent from the ram block-breaking
     * config because it is part of the breathing behavior, but still respects mobGriefing and
     * Forge's per-block destruction hook so protection mods can veto the change.
     */
    private void breakBreathingIceAboveHead() {
        if (level().isClientSide || !surfacing || !ForgeEventFactory.getMobGriefingEvent(level(), this)) {
            return;
        }

        AABB breakBox = headPart.getBoundingBox()
                .inflate(0.35D, 0.12D, 0.35D)
                .expandTowards(0.0D, 1.6D, 0.0D);
        int minX = Mth.floor(breakBox.minX);
        int minY = Mth.floor(breakBox.minY);
        int minZ = Mth.floor(breakBox.minZ);
        int maxX = Mth.floor(breakBox.maxX);
        int maxY = Mth.floor(breakBox.maxY);
        int maxZ = Mth.floor(breakBox.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = level().getBlockState(pos);
                    if (!isBreathingIce(state)
                            || !new AABB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).intersects(breakBox)) {
                        continue;
                    }
                    BlockPos target = pos.immutable();
                    if (ForgeEventFactory.onEntityDestroyBlock(this, target, state)) {
                        level().destroyBlock(target, false);
                    }
                }
            }
        }
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
        double sweepDistance = Math.max(0.7D, getDeltaMovement().length() + 0.35D);
        // 每 tick 求值一次：配置开关 + mobGriefing 事件（后者让其他 mod 能按生物单独放行/拦截）。
        boolean mayBreak = FmbCommonConfig.whaleBreaksBlocks() && ForgeEventFactory.getMobGriefingEvent(level(), this);

        for (double distance = 0.35D; distance <= sweepDistance; distance += 0.35D) {
            if (processRamBlockSlice(headBox.move(normalized.scale(distance)), mayBreak)) {
                stunFromCollision(getDeltaMovement());
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
                            return playHardRamImpact(pos.immutable(), state);
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
                    if (hardness >= 0.0F && hardness < OBSIDIAN_HARDNESS && new AABB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).intersects(box)) {
                        BlockPos target = pos.immutable();
                        // 领地/保护类 mod 的拦截入口；被拒绝的实心方块同样把鲸鱼撞晕。
                        if (!ForgeEventFactory.onEntityDestroyBlock(this, target, state)) {
                            if (!state.getCollisionShape(level(), target).isEmpty()) {
                                return playHardRamImpact(target, state);
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

    private boolean playHardRamImpact(BlockPos pos, BlockState state) {
        var soundType = state.getSoundType(level(), pos, this);
        level().playSound(null, pos, soundType.getHitSound(), getSoundSource(), Math.max(2.0F, soundType.getVolume() * 2.0F), Math.max(0.45F, soundType.getPitch() * 0.62F));
        return true;
    }

    private void stunFromCollision(Vec3 incomingMovement) {
        stunnedTicks = STUN_DURATION;
        setStunned(true);
        navigation.stop();
        surfacing = false;
        setAction(ACTION_IDLE);
        // Recoil and water drag make the impact readable without freezing in a single frame.
        setDeltaMovement(incomingMovement.scale(-0.14D));
        hasImpulse = true;
        attackCooldown = Math.max(attackCooldown, STUN_DURATION);
    }

    private void pushEntitiesFromParts() {
        for (WhalePart part : bodyParts) {
            for (Entity entity : level().getEntities(part, part.getBoundingBox().inflate(0.1D), candidate -> candidate.isPushable() && candidate != this && !(candidate instanceof WhalePart))) {
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
        float pitch = getVisualPitch(1.0F);
        Vec3 forward = Vec3.directionFromRotation(pitch, yBodyRot).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
        Vec3 up = right.cross(forward).normalize();
        return headPart.getBoundingBox().getCenter()
                .add(up.scale(headPart.getBbHeight() * 0.5D + 0.15D))
                .subtract(forward.scale(2.0D));
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

    boolean hurtFromPart(WhalePart part, DamageSource source, float amount) {
        WhalePart previous = damagePartContext;
        damagePartContext = part;
        try {
            return hurt(source, amount);
        } finally {
            damagePartContext = previous;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this || source.getEntity() instanceof WhalePart) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && source.getEntity() instanceof LivingEntity attacker && !(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            setTarget(attacker);
        }
        return hurt;
    }

    /**
     * 机体坐标系：右 / 上 / 前，插着的投射物按这三轴归一化记录。
     */
    private Vec3[] bodyAxes() {
        Vec3 forward = Vec3.directionFromRotation(getXRot(), yBodyRot).normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
        return new Vec3[]{right, right.cross(forward).normalize(), forward};
    }

    private int getPartIndex(WhalePart part) {
        for (int i = 0; i < bodyParts.length; i++) {
            if (bodyParts[i] == part) {
                return i;
            }
        }
        return 1;
    }

    @Nullable
    private WhalePart findClosestBodyPart(Vec3 point) {
        WhalePart closest = null;
        double bestDistance = Double.MAX_VALUE;
        for (WhalePart part : bodyParts) {
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
        return new Vec3(Mth.clamp(point.x, box.minX, box.maxX), Mth.clamp(point.y, box.minY, box.maxY), Mth.clamp(point.z, box.minZ, box.maxZ));
    }

    @Override
    protected void dropFromLootTable(DamageSource source, boolean causedByPlayer) {
        if (!FmbCommonConfig.whaleDropsEnabled()) {
            return;
        }

        capturedWhaleLoot.clear();
        capturingWhaleLoot = true;
        try {
            // Keep vanilla's complete entity LootContext and Forge loot modifiers. spawnAtLocation
            // is intercepted only for this call so the resulting stacks can use the full body.
            super.dropFromLootTable(source, causedByPlayer);
        } finally {
            capturingWhaleLoot = false;
        }

        if (!capturedWhaleLoot.isEmpty()) {
            List<ItemStack> drops = new ArrayList<>(capturedWhaleLoot);
            capturedWhaleLoot.clear();
            scatterAlongBody(drops);
        }
    }

    @Override
    @Nullable
    public ItemEntity spawnAtLocation(ItemStack stack, float yOffset) {
        if (capturingWhaleLoot && !stack.isEmpty()) {
            capturedWhaleLoot.add(stack.copy());
            return null;
        }
        return super.spawnAtLocation(stack, yOffset);
    }

    /**
     * Spread death drops over the live multipart hitboxes so the whole curved body is used,
     * including the fins and fluke, instead of concentrating everything near the entity origin.
     */
    private void scatterAlongBody(List<ItemStack> drops) {
        List<ItemStack> spreadDrops = new ArrayList<>();
        for (ItemStack stack : drops) {
            for (int i = 0; i < stack.getCount(); i++) {
                ItemStack single = stack.copy();
                single.setCount(1);
                spreadDrops.add(single);
            }
        }

        int count = spreadDrops.size();
        if (count <= 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            // Five is coprime with the eight body parts, so consecutive stacks visit every part
            // before repeating instead of clustering one item type at the head or tail.
            int partIndex = count == 1 ? 1 : (i * 5) % bodyParts.length;
            AABB box = bodyParts[partIndex].getBoundingBox();
            double insetX = Math.min(0.35D, box.getXsize() * 0.20D);
            double insetZ = Math.min(0.35D, box.getZsize() * 0.20D);
            double dropX = Mth.lerp(random.nextDouble(), box.minX + insetX, box.maxX - insetX);
            double dropY = box.maxY + 0.15D + random.nextDouble() * 0.35D;
            double dropZ = Mth.lerp(random.nextDouble(), box.minZ + insetZ, box.maxZ - insetZ);
            Vec3 dropPosition = new Vec3(dropX, dropY, dropZ);
            ItemEntity item = new ItemEntity(level(), dropPosition.x, dropPosition.y, dropPosition.z, spreadDrops.get(i));
            item.setDefaultPickUpDelay();

            Vec3 outward = dropPosition.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (outward.lengthSqr() < 1.0E-5D) {
                outward = Vec3.directionFromRotation(0.0F, yBodyRot + 90.0F);
            } else {
                outward = outward.normalize();
            }
            double outwardSpeed = 0.08D + random.nextDouble() * 0.08D;
            item.setDeltaMovement(outward.scale(outwardSpeed).add(0.0D, 0.14D + random.nextDouble() * 0.08D, 0.0D));
            Collection<ItemEntity> capturedDrops = captureDrops();
            if (capturedDrops != null) {
                capturedDrops.add(item);
            } else {
                level().addFreshEntity(item);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Beached", isBeached());
        tag.putInt("BreatheCountdown", breatheCountdown);
        tag.putInt("Moistness", moistness);
        tag.putInt("StunnedTicks", stunnedTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBeached(tag.getBoolean("Beached"));
        breatheCountdown = tag.contains("BreatheCountdown") ? tag.getInt("BreatheCountdown") : random.nextInt(900, 1801);
        moistness = tag.contains("Moistness") ? tag.getInt("Moistness") : TOTAL_MOISTNESS;
        stunnedTicks = Math.max(0, tag.getInt("StunnedTicks"));
        setStunned(stunnedTicks > 0);
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
        refreshBodyPartsForSpawn();
        if (!level.isUnobstructed(this)) {
            return false;
        }
        for (WhalePart part : bodyParts) {
            if (!level.isUnobstructed(part)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isStunned()) {
            if (isEffectiveAi()) {
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().multiply(0.72D, 0.82D, 0.72D));
            }
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
        return ModSounds.WHALE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WHALE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WHALE_DEATH.get();
    }

    private final class SurfaceToBreatheGoal extends Goal {
        private static final int MAX_DESCENT_TICKS = 100;
        private static final double DESCENT_DEPTH_BELOW_SURFACE = 6.0D;
        private BlockPos surfaceAir;
        private boolean blowStarted;
        private boolean descending;
        private boolean emergencyBreathing;
        private int stalledTicks;
        private int descentTicks;
        private double lastY;

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
                    // Once breathing has begun, always finish the return below the surface before
                    // handing control back to normal swimming or combat goals.
                    && (blowStarted || getTarget() == null || emergencyBreathing);
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            surfacing = true;
            blowStarted = false;
            descending = false;
            descentTicks = 0;
            stalledTicks = 0;
            lastY = getY();
            navigation.stop();
        }

        @Override
        public void tick() {
            if (descending) {
                descendFromSurface();
                return;
            }
            if (blowStarted) {
                if (getAction() != ACTION_BLOW) {
                    descending = true;
                    descentTicks = 0;
                    descendFromSurface();
                    return;
                }
                Vec3 movement = getDeltaMovement();
                Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot()).scale(0.006D);
                setDeltaMovement(movement.x * 0.88D + forward.x, Math.min(movement.y, 0.0D), movement.z * 0.88D + forward.z);
                setXRot(Mth.approachDegrees(getXRot(), 0.0F, 1.5F));
                return;
            }
            riseToSurface();
            if (!isEyeInFluid(FluidTags.WATER)) {
                blowStarted = true;
                setAirSupply(getMaxAirSupply());
                setAction(ACTION_BLOW);
                playSound(SoundEvents.PLAYER_BREATH, 1.6F, 0.55F);
            }
        }

        @Override
        public void stop() {
            boolean breathed = blowStarted;
            surfacing = false;
            surfaceAir = null;
            blowStarted = false;
            descending = false;
            descentTicks = 0;
            emergencyBreathing = false;
            // If another goal interrupted the ascent, retry as soon as it is safe instead of
            // silently postponing breathing for another full interval.
            breatheCountdown = breathed ? random.nextInt(900, 1801) : 0;
            if (getAction() == ACTION_BLOW) {
                setAction(ACTION_IDLE);
            }
        }

        private void descendFromSurface() {
            descentTicks++;
            setXRot(Mth.approachDegrees(getXRot(), 18.0F, 1.4F));

            Vec3 movement = getDeltaMovement();
            Vec3 forward = Vec3.directionFromRotation(getXRot(), yBodyRot);
            double horizontalX = movement.x * 0.82D + forward.x * 0.014D;
            double horizontalZ = movement.z * 0.82D + forward.z * 0.014D;
            double verticalSpeed = Mth.clamp(movement.y * 0.82D + forward.y * 0.018D - 0.018D, -0.11D, -0.02D);
            setDeltaMovement(horizontalX, verticalSpeed, horizontalZ);
            hasImpulse = true;

            double targetEyeY = surfaceAir.getY() - DESCENT_DEPTH_BELOW_SURFACE;
            if (getEyeY() <= targetEyeY || descentTicks >= MAX_DESCENT_TICKS || horizontalCollision) {
                surfacing = false;
            }
        }

        private void riseToSurface() {
            // A frozen ocean surface must not trap the whale below the ice.  Clear only the
            // three vanilla ice blocks requested for breathing; normal terrain is untouched.
            breakBreathingIceAboveHead();

            double targetX = surfaceAir.getX() + 0.5D;
            double targetZ = surfaceAir.getZ() + 0.5D;
            double dx = targetX - getX();
            double dz = targetZ - getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            if (horizontalDistance > 0.15D) {
                float targetYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
                turnToward(targetYaw, 2.0F, 0.18F);
            }
            setXRot(Mth.approachDegrees(getXRot(), emergencyBreathing ? -24.0F : -18.0F, 1.2F));

            Vec3 movement = getDeltaMovement();
            double horizontalAcceleration = emergencyBreathing ? 0.012D : 0.008D;
            double horizontalX = horizontalDistance > 1.0E-4D ? dx / horizontalDistance * horizontalAcceleration : 0.0D;
            double horizontalZ = horizontalDistance > 1.0E-4D ? dz / horizontalDistance * horizontalAcceleration : 0.0D;
            double riseAcceleration = emergencyBreathing ? 0.055D : 0.035D;
            double maximumRiseSpeed = emergencyBreathing ? 0.13D : 0.085D;
            double verticalSpeed = Mth.clamp(movement.y * 0.72D + riseAcceleration, 0.028D, maximumRiseSpeed);

            if (getY() <= lastY + 0.002D) {
                stalledTicks++;
            } else {
                stalledTicks = 0;
            }
            lastY = getY();
            if (stalledTicks > 25) {
                verticalSpeed = maximumRiseSpeed;
            }

            setDeltaMovement(movement.x * 0.82D + horizontalX, verticalSpeed, movement.z * 0.82D + horizontalZ);
            hasImpulse = true;
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

            // Frozen oceans can have ice directly on top of the water.  Treat a contiguous
            // column of ice / packed ice / blue ice as a valid breathing route and target the
            // first air block above it; riseToSurface() will physically break the ice on the way.
            if (isBreathingIce(level().getBlockState(cursor))) {
                while (cursor.getY() < level().getMaxBuildHeight() - 1
                        && isBreathingIce(level().getBlockState(cursor))) {
                    cursor.move(0, 1, 0);
                }
                return level().getBlockState(cursor).isAir() ? cursor.immutable() : null;
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
            return !isBeached() && !isStunned() && !surfacing && getTarget() == null && swimTarget != null && Vec3.atCenterOf(swimTarget).distanceToSqr(position()) > 6.25D && travelTicks < 420;
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
                BlockPos target = cursor.above(4 + random.nextInt(5)).immutable();
                if (hasVerticalWaterClearance(target)) {
                    return target;
                }
            }
            return null;
        }

        private boolean hasVerticalWaterClearance(BlockPos bottom) {
            for (int y = 0; y <= 8; y++) {
                if (!level().getFluidState(bottom.above(y)).is(FluidTags.WATER)) {
                    return false;
                }
            }
            return true;
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
            playSound(ModSounds.WHALE_ANGRY.get(), 1.8F, 1.0F);
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
                Vec3 targetDirection = target.getEyePosition().subtract(getEyePosition());
                if (targetDirection.lengthSqr() > 1.0E-5D) {
                    targetDirection = targetDirection.normalize();
                    double horizontal = Math.sqrt(targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z);
                    float targetYaw = (float) (Mth.atan2(targetDirection.z, targetDirection.x) * Mth.RAD_TO_DEG) - 90.0F;
                    float targetPitch = Mth.clamp((float) (-Mth.atan2(targetDirection.y, horizontal) * Mth.RAD_TO_DEG), -30.0F, 30.0F);
                    turnToward(targetYaw, 4.5F, 0.45F);
                    setXRot(Mth.approachDegrees(getXRot(), targetPitch, 2.0F));
                }

                // Accelerate along the visible body's axis so turns form an arc instead of a
                // sideways slide toward the target.
                Vec3 driveDirection = Vec3.directionFromRotation(getXRot(), yBodyRot).normalize();
                if (processRamBlocks(driveDirection)) {
                    phase = DONE;
                    return;
                }
                setDeltaMovement(getDeltaMovement().scale(0.68D).add(driveDirection.scale(0.22D)));

                if (headPart.getBoundingBox().inflate(0.7D).intersects(target.getBoundingBox())) {
                    DamageSource ramSource = damageSources().mobAttack(WhaleEntity.this);
                    boolean blockedByShield = target.isDamageSourceBlocked(ramSource);
                    Item blockingItem = blockedByShield ? target.getUseItem().getItem() : null;
                    Vec3 incomingMovement = getDeltaMovement();
                    target.hurt(ramSource, (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
                    if (blockedByShield) {
                        if (target instanceof Player player && blockingItem != null) {
                            player.getCooldowns().addCooldown(blockingItem, SHIELD_DISABLE_DURATION);
                            player.stopUsingItem();
                            level().broadcastEntityEvent(player, (byte) 30);
                        }
                        stunFromCollision(incomingMovement);
                        phase = DONE;
                        return;
                    }
                    target.push(
                            driveDirection.x * RAM_KNOCKBACK_HORIZONTAL,
                            RAM_KNOCKBACK_VERTICAL,
                            driveDirection.z * RAM_KNOCKBACK_HORIZONTAL
                    );
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
