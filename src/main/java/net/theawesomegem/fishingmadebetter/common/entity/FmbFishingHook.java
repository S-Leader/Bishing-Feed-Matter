package net.theawesomegem.fishingmadebetter.common.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishData.FishingLiquid;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.data.FishPopulationSavedData;
import net.theawesomegem.fishingmadebetter.common.item.BaitBucketItem;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.network.ReelingInput;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;

public class FmbFishingHook extends FishingHook {
    private static final ResourceLocation FMB_COMBINED_LOOT = new ResourceLocation(Constants.MOD_ID, "fishing_combined");
    private static final int BASE_TREASURE_CHANCE = 15;
    private static final EntityDataAccessor<Integer> HUD_REEL_AMOUNT = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_REEL_TARGET = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_ERROR_VARIANCE = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_LINE_BREAK = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_FISH_DISTANCE = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_FISH_DEPTH = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_BACKGROUND_DIMENSION = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_BACKGROUND_CAVE = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_BACKGROUND_TIME = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_BACKGROUND_LIQUID = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUD_BACKGROUND_BIOME = SynchedEntityData.defineId(FmbFishingHook.class, EntityDataSerializers.INT);
    private final HookLiquid hookLiquid;
    private State state = State.FLYING;
    private Entity hookedIn;
    private int life;
    private int nibble;
    private int timeUntilBite;
    private int failedBiteAttempts;
    private int reelRange = 40;
    private int reelSpeed = 2;
    private int biteRateModifier;
    private int weightModifier;
    private int treasureModifier;
    private int tuggingReduction;
    private int tensioningModifier;
    private int varianceModifier;
    private String baitId = "";
    private int reelAmount = 500;
    private int reelTarget = 500;
    private int errorVariance = 80;
    private int lineBreak;
    private int fishMomentum;
    private int fishTugging;
    private int tensionMomentum;
    private String activeFishId = "";
    private int activeFishWeight;
    private int fishDistance;
    private int fishDeepLevel;
    private int fishDistanceTime;
    private ReelingInput reelingInput = ReelingInput.NONE;

    public FmbFishingHook(EntityType<? extends FishingHook> entityType, Level level, HookLiquid hookLiquid) {
        super(entityType, level);
        this.hookLiquid = hookLiquid;
    }

    protected FmbFishingHook(EntityType<? extends FishingHook> entityType, Player player, Level level, HookLiquid hookLiquid, ItemStack rodStack) {
        this(entityType, level, hookLiquid);
        setOwner(player);
        applyRodModifiers(rodStack);
        String rodBait = BetterFishingRodItem.getBaitItem(rodStack);
        baitId = rodBait == null ? "" : rodBait;

        float xRot = player.getXRot();
        float yRot = player.getYRot();
        float cosYaw = Mth.cos(-yRot * Mth.DEG_TO_RAD - Mth.PI);
        float sinYaw = Mth.sin(-yRot * Mth.DEG_TO_RAD - Mth.PI);
        float negPitchCos = -Mth.cos(-xRot * Mth.DEG_TO_RAD);
        float sinPitch = Mth.sin(-xRot * Mth.DEG_TO_RAD);
        double x = player.getX() - sinYaw * 0.3;
        double y = player.getEyeY();
        double z = player.getZ() - cosYaw * 0.3;
        moveTo(x, y, z, yRot, xRot);

        Vec3 movement = new Vec3(-sinYaw, Mth.clamp(-(sinPitch / negPitchCos), -5.0F, 5.0F), -cosYaw);
        double movementLength = movement.length();
        movement = movement.multiply(
                0.6 / movementLength + random.triangle(0.5, 0.0103365),
                0.6 / movementLength + random.triangle(0.5, 0.0103365),
                0.6 / movementLength + random.triangle(0.5, 0.0103365)
        );
        setDeltaMovement(movement);
        setYRot((float) (Mth.atan2(movement.x, movement.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(movement.y, movement.horizontalDistance()) * Mth.RAD_TO_DEG));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    public HookLiquid getHookLiquid() {
        return hookLiquid;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HUD_REEL_AMOUNT, 0);
        entityData.define(HUD_REEL_TARGET, 0);
        entityData.define(HUD_ERROR_VARIANCE, 80);
        entityData.define(HUD_LINE_BREAK, 0);
        entityData.define(HUD_FISH_DISTANCE, 0);
        entityData.define(HUD_FISH_DEPTH, 0);
        entityData.define(HUD_BACKGROUND_DIMENSION, 0);
        entityData.define(HUD_BACKGROUND_CAVE, 0);
        entityData.define(HUD_BACKGROUND_TIME, 0);
        entityData.define(HUD_BACKGROUND_LIQUID, 0);
        entityData.define(HUD_BACKGROUND_BIOME, 6);
    }

    public int getHudReelAmount() {
        return entityData.get(HUD_REEL_AMOUNT);
    }

    public int getHudReelTarget() {
        return entityData.get(HUD_REEL_TARGET);
    }

    public int getHudErrorVariance() {
        return entityData.get(HUD_ERROR_VARIANCE);
    }

    public int getHudLineBreak() {
        return entityData.get(HUD_LINE_BREAK);
    }

    public int getHudFishDistance() {
        return entityData.get(HUD_FISH_DISTANCE);
    }

    public int getHudFishDepth() {
        return entityData.get(HUD_FISH_DEPTH);
    }

    public int getHudBackgroundDimension() {
        return entityData.get(HUD_BACKGROUND_DIMENSION);
    }

    public int getHudBackgroundCave() {
        return entityData.get(HUD_BACKGROUND_CAVE);
    }

    public int getHudBackgroundTime() {
        return entityData.get(HUD_BACKGROUND_TIME);
    }

    public int getHudBackgroundLiquid() {
        return entityData.get(HUD_BACKGROUND_LIQUID);
    }

    public int getHudBackgroundBiome() {
        return entityData.get(HUD_BACKGROUND_BIOME);
    }

    public void setReelingInput(ReelingInput reelingInput) {
        this.reelingInput = reelingInput;
    }

    @Override
    public void tick() {
        super.baseTick();
        Player player = getPlayerOwner();
        if (player == null) {
            discard();
            return;
        }
        if (!level().isClientSide && shouldStopCustomFishing(player)) {
            return;
        }

        if (onGround()) {
            life++;
            if (life >= 1200) {
                discard();
                return;
            }
        } else {
            life = 0;
        }

        BlockPos blockPos = blockPosition();
        float liquidHeight = getTargetLiquidHeight(blockPos);
        boolean inTargetLiquid = liquidHeight > 0.0F;
        if (state == State.FLYING) {
            if (hookedIn != null) {
                setDeltaMovement(Vec3.ZERO);
                state = State.HOOKED_IN_ENTITY;
                return;
            }
            if (inTargetLiquid) {
                setDeltaMovement(getDeltaMovement().multiply(0.3, 0.2, 0.3));
                state = State.BOBBING;
                return;
            }
            checkCustomCollision();
        } else if (state == State.HOOKED_IN_ENTITY) {
            tickHookedEntity();
            return;
        } else if (state == State.BOBBING) {
            tickBobbing(blockPos, liquidHeight, inTargetLiquid);
        }

        if (!inTargetLiquid) {
            setDeltaMovement(getDeltaMovement().add(0.0, -0.03, 0.0));
        }
        move(MoverType.SELF, getDeltaMovement());
        updateRotation();
        if (state == State.FLYING && (onGround() || horizontalCollision)) {
            setDeltaMovement(Vec3.ZERO);
        }
        setDeltaMovement(getDeltaMovement().scale(0.92));
        reapplyPosition();
    }

    @Override
    public int retrieve(ItemStack stack) {
        Player player = getPlayerOwner();
        if (!level().isClientSide && player != null && !shouldStopCustomFishing(player)) {
            int damage = 0;
            if (hookedIn != null) {
                pullEntity(hookedIn);
                level().broadcastEntityEvent(this, (byte) 31);
                damage = hookedIn instanceof ItemEntity ? 3 : 5;
            } else if (nibble > 0 && isFishCloseEnough() && tryCatchFish(player, stack)) {
                // 1.12.2 only required the fish to be reeled close enough; the
                // target did not also have to overlap on the exact retrieval tick.
                damage = 1;
            }
            if (onGround()) {
                damage = 2;
            }
            discard();
            return damage;
        }
        return 0;
    }

    private boolean shouldStopCustomFishing(Player player) {
        boolean mainHandRod = player.getMainHandItem().getItem() instanceof BetterFishingRodItem;
        boolean offHandRod = player.getOffhandItem().getItem() instanceof BetterFishingRodItem;
        double maxDistance = Math.max(8, reelRange);
        if (!player.isRemoved() && player.isAlive() && (mainHandRod || offHandRod) && distanceToSqr(player) <= maxDistance * maxDistance) {
            return false;
        }
        discard();
        return true;
    }

    private void applyRodModifiers(ItemStack rodStack) {
        ReelItem reel = BetterFishingRodItem.getReelItem(rodStack);
        BobberItem bobber = BetterFishingRodItem.getBobberItem(rodStack);
        HookItem hook = BetterFishingRodItem.getHookItem(rodStack);
        this.reelRange = Math.max(8, reel.getReelRange());
        this.reelSpeed = Math.max(1, reel.getReelSpeed());
        this.biteRateModifier = hook.getBiteRateModifier() + EnchantmentHelper.getFishingSpeedBonus(rodStack) * 15;
        this.weightModifier = hook.getWeightModifier();
        this.treasureModifier = hook.getTreasureModifier();
        this.tuggingReduction = hook.getTuggingReduction();
        this.tensioningModifier = bobber.getTensioningModifier();
        this.varianceModifier = bobber.getVarianceModifier();
    }

    private float getTargetLiquidHeight(BlockPos blockPos) {
        if (hookLiquid == HookLiquid.VOID) {
            return getY() < 0.0D ? 0.8888888F : 0.0F;
        }

        FluidState fluidState = level().getFluidState(blockPos);
        if (hookLiquid == HookLiquid.WATER && fluidState.is(FluidTags.WATER)) {
            return fluidState.getHeight(level(), blockPos);
        }
        if (hookLiquid == HookLiquid.LAVA && fluidState.is(FluidTags.LAVA)) {
            return fluidState.getHeight(level(), blockPos);
        }
        return 0.0F;
    }

    private void checkCustomCollision() {
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        onHit(hitResult);
    }

    private void tickHookedEntity() {
        if (hookedIn == null) {
            state = State.FLYING;
            return;
        }
        if (!hookedIn.isRemoved() && hookedIn.level().dimension() == level().dimension()) {
            setPos(hookedIn.getX(), hookedIn.getY(0.8), hookedIn.getZ());
        } else {
            hookedIn = null;
            state = State.FLYING;
        }
    }

    private void tickBobbing(BlockPos blockPos, float liquidHeight, boolean inTargetLiquid) {
        Vec3 movement = getDeltaMovement();
        double liquidOffset = getY() + movement.y - blockPos.getY() - liquidHeight;
        if (Math.abs(liquidOffset) < 0.01D) {
            liquidOffset += Math.signum(liquidOffset) * 0.1D;
        }
        setDeltaMovement(movement.x * 0.9D, movement.y - liquidOffset * random.nextFloat() * 0.2D, movement.z * 0.9D);
        if (!level().isClientSide && inTargetLiquid) {
            tickBiteTimer();
        }
        if (inTargetLiquid && hookLiquid != HookLiquid.VOID) {
            clearFire();
        }
    }

    private void tickBiteTimer() {
        if (nibble > 0) {
            tickFishTugging();
            tickReelingInput();
            tickFishDistance();
            tickLineBreak();

            // 1.12.2 continuously forced the vanilla bite interval back to 20
            // while the minigame was active. Its fish-time countdown was disabled,
            // so a hooked fish only escaped when lineBreak reached 60.
            if (lineBreak >= 60) {
                nibble = 0;
                notifyLineSnapped();
            } else {
                nibble = 20;
            }
            updateHudData();
            return;
        }
        activeFishId = "";
        if (timeUntilBite <= 0) {
            int minDelay = adjustedBiteDelay(100);
            int maxDelay = adjustedBiteDelay(600);
            timeUntilBite = Mth.nextInt(random, minDelay, Math.max(minDelay, maxDelay));
        }

        timeUntilBite -= reelSpeed;
        int delayProgress = Mth.clamp(1000 - timeUntilBite * 1000 / Math.max(1, adjustedBiteDelay(600)), 0, 1000);
        reelAmount = delayProgress;
        reelTarget = 500;
        errorVariance = 80;
        // 1.12.2 kept lineBreak at zero until a fish was actually hooked.
        lineBreak = 0;
        fishDistance = 0;
        fishDeepLevel = 0;
        updateHudData();
        if (timeUntilBite <= 0 && beginFishBite()) {
            Player player = getPlayerOwner();
            if (player != null) {
                playBiteFeedback(player);
                consumeAndRefillRodBait(player);
            }
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.08D * random.nextFloat() * random.nextFloat(), 0.0D));
            // Start every hooked fish exactly in the middle of the minigame.
            // The position/error bar and target marker must overlap on the first
            // frame so a bite can never begin already outside the valid zone.
            reelAmount = 500;
            reelTarget = 500;
            lineBreak = 0;
            fishMomentum = 0;
            fishTugging = 0;
            tensionMomentum = 0;
            fishDistanceTime = 0;
            // 1.12.2 keeps the bite alive by repeatedly setting the interval to 20.
            nibble = 20;
            updateHudData();
        }
    }

    private boolean beginFishBite() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        ChunkPos chunkPos = level().getChunk(blockPosition()).getPos();
        FishPopulationSavedData.PopulationData population = FishPopulationSavedData.get(serverLevel)
                .selectRandomFish(serverLevel, chunkPos, level().getGameTime(), this::canCatchFish, this::catchWeight);
        if (population == null) {
            notifyEmptyBiteAttempt();
            timeUntilBite = adjustedBiteDelay(100);
            return false;
        }

        FishData fishData = FishDataRegistry.get(population.fishId());
        if (fishData == null) {
            notifyEmptyBiteAttempt();
            timeUntilBite = adjustedBiteDelay(100);
            return false;
        }

        failedBiteAttempts = 0;
        activeFishId = population.fishId();
        activeFishWeight = Mth.nextInt(random, fishData.minWeight(), fishData.maxWeight());
        int hookedFishDepth = Math.max(0, Mth.nextInt(random, fishData.minDeepLevel(), fishData.maxDeepLevel()));
        // 1.12.2 stores fishDistance as progress along the reel's total range.
        // A fish starts at (reelRange - its depth), giving it a buffer before
        // fishDistance can fall to zero and begin damaging the line.
        fishDistance = Math.max(0, (reelRange - hookedFishDepth) * 10);
        fishDeepLevel = Math.max(10, reelRange * 10);
        errorVariance = Mth.clamp((2 + Mth.nextInt(random, fishData.minErrorVariance(), fishData.maxErrorVariance()) + varianceModifier) * 10, 20, 220);
        return true;
    }

    private void consumeAndRefillRodBait(Player player) {
        ItemStack rodStack = findBetterFishingRod(player);
        if (rodStack.isEmpty() || !BetterFishingRodItem.hasBait(rodStack)) {
            return;
        }

        BetterFishingRodItem.removeBait(rodStack);
        ItemStack baitBucket = findBaitBucket(player);
        if (baitBucket.isEmpty() || BaitBucketItem.getBaitId(baitBucket).isEmpty()) {
            return;
        }

        BetterFishingRodItem.setBaitItem(rodStack, BaitBucketItem.getBaitId(baitBucket));
        BetterFishingRodItem.setBaitMetadata(rodStack, BaitBucketItem.getBaitMetadata(baitBucket));
        BetterFishingRodItem.setBaitDisplayName(rodStack, BaitBucketItem.getBaitDisplayName(baitBucket));
        BaitBucketItem.setBaitCount(baitBucket, BaitBucketItem.getBaitCount(baitBucket) - 1);
        if (BaitBucketItem.getBaitCount(baitBucket) <= 0) {
            BaitBucketItem.removeBait(baitBucket);
        }
    }

    private static ItemStack findBetterFishingRod(Player player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() instanceof BetterFishingRodItem) {
            return mainHand;
        }
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        return offHand.getItem() instanceof BetterFishingRodItem ? offHand : ItemStack.EMPTY;
    }

    private static ItemStack findBaitBucket(Player player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() instanceof BaitBucketItem) {
            return mainHand;
        }
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        return offHand.getItem() instanceof BaitBucketItem ? offHand : ItemStack.EMPTY;
    }

    private void tickFishTugging() {
        int targetTugging = fishTugging;
        if (targetTugging == 0 || random.nextInt(12) == 0) {
            int tugDirection = random.nextInt(3) - 1;
            int tugStrength = Math.max(0, Mth.nextInt(random, -2, 6) - tuggingReduction);
            targetTugging = tugDirection * tugStrength;
        } else if (random.nextInt(20) == 0) {
            targetTugging = 0;
        }

        fishTugging = targetTugging;
        fishMomentum += Integer.signum(fishTugging - fishMomentum);
        reelTarget = Mth.clamp(reelTarget + fishMomentum, 30, 970);
    }

    private void tickReelingInput() {
        int targetTension = 0;
        if (reelingInput == ReelingInput.REEL_IN) {
            targetTension = 8 + reelSpeed * 2 + tensioningModifier;
        } else if (reelingInput == ReelingInput.REEL_OUT) {
            targetTension = -(8 + tensioningModifier);
        }

        tensionMomentum += Integer.signum(targetTension - tensionMomentum);
        reelAmount += tensionMomentum;
        reelAmount = Mth.clamp(reelAmount, 0, 1000);

    }

    private void tickFishDistance() {
        if (fishDistanceTime > 0) {
            fishDistanceTime--;
            return;
        }

        fishDistanceTime = Math.min(10, 1 + activeFishWeight / 100);
        int distanceChange = isReelAmountOnTarget() ? reelSpeed : -1;
        // 1.12.2 adds reelSpeed or -1 directly. The modern port had an extra
        // *10 here, making both reeling and losing ground ten times faster.
        fishDistance = Mth.clamp(fishDistance + distanceChange, 0, fishDeepLevel);
    }

    private void tickLineBreak() {
        // Exact 1.12.2 rule: being outside the target only makes fishDistance
        // fall. The line itself starts breaking only after that distance reaches 0.
        lineBreak = Mth.clamp(lineBreak + (fishDistance == 0 ? 1 : -1), 0, 60);
    }

    private boolean isFishCloseEnough() {
        return fishDeepLevel <= 0 || fishDistance >= fishDeepLevel - 10;
    }

    private boolean isReelAmountOnTarget() {
        return Math.abs(reelAmount - reelTarget) <= errorVariance;
    }

    private void updateHudData() {
        entityData.set(HUD_REEL_AMOUNT, reelAmount);
        entityData.set(HUD_REEL_TARGET, reelTarget);
        entityData.set(HUD_ERROR_VARIANCE, errorVariance);
        entityData.set(HUD_LINE_BREAK, lineBreak);
        entityData.set(HUD_FISH_DISTANCE, fishDistance);
        entityData.set(HUD_FISH_DEPTH, fishDeepLevel);
        entityData.set(HUD_BACKGROUND_DIMENSION, backgroundDimension());
        entityData.set(HUD_BACKGROUND_CAVE, getY() < 56.0D ? 1 : 0);
        entityData.set(HUD_BACKGROUND_TIME, level().isDay() ? 0 : 1);
        entityData.set(HUD_BACKGROUND_LIQUID, backgroundLiquid());
        entityData.set(HUD_BACKGROUND_BIOME, backgroundBiomeIndex());
    }

    private int backgroundDimension() {
        if (level().dimension() == Level.END) {
            return 1;
        }
        if (level().dimension() == Level.NETHER) {
            return -1;
        }
        return 0;
    }

    private int backgroundLiquid() {
        return switch (hookLiquid) {
            case LAVA -> 1;
            case VOID -> 2;
            case WATER -> 0;
        };
    }

    private int backgroundBiomeIndex() {
        var biome = level().getBiome(blockPosition());
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            return 1;
        }
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_SAVANNA)) {
            return 2;
        }
        if (biome.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS) || biome.is(BiomeTags.SPAWNS_WHITE_RABBITS)) {
            return 3;
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL)) {
            return 4;
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA)) {
            return 5;
        }
        if (biome.is(BiomeTags.IS_RIVER)) {
            return 6;
        }
        String biomePath = biome.unwrapKey().map(key -> key.location().getPath()).orElse("");
        if (biomePath.contains("swamp") || biomePath.contains("mangrove")) {
            return 7;
        }
        if (biomePath.contains("mushroom")) {
            return 8;
        }
        if (biomePath.contains("dark_forest") || biomePath.contains("deep_dark") || biomePath.contains("soul")) {
            return 9;
        }
        return 6;
    }

    private int adjustedBiteDelay(int baseDelay) {
        float multiplier = Math.max(0.2F, 1.0F - biteRateModifier / 100.0F);
        return Math.max(20, Math.round(baseDelay * multiplier));
    }

    private void notifyEmptyBiteAttempt() {
        failedBiteAttempts++;
        if (failedBiteAttempts % 3 == 0) {
            Player player = getPlayerOwner();
            if (player != null) {
                player.displayClientMessage(Component.translatable("notif.fishingmadebetter.failure.empty"), true);
            }
        }
    }

    private void notifyLineSnapped() {
        Player player = getPlayerOwner();
        if (player != null) {
            // The active 1.12.2 logic only ended the minigame through line break;
            // its fish-time failure branch was commented out.
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.failure.snap"), true);
        }
    }

    private void playBiteFeedback(Player player) {
        player.displayClientMessage(Component.translatable("notif.fishingmadebetter.bite"), true);
        if (hookLiquid == HookLiquid.LAVA) {
            level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL_LAVA, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (hookLiquid == HookLiquid.VOID) {
            level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE, getX(), getY() + 0.5D, getZ(), (int) (1.0F + getBbWidth() * 20.0F), getBbWidth(), 0.0D, getBbWidth(), 0.2D);
            serverLevel.sendParticles(ParticleTypes.FISHING, getX(), getY() + 0.5D, getZ(), (int) (1.0F + getBbWidth() * 20.0F), getBbWidth(), 0.0D, getBbWidth(), 0.2D);
        }
    }

    private boolean tryCatchFish(Player player, ItemStack rodStack) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        ChunkPos chunkPos = level().getChunk(blockPosition()).getPos();
        if (activeFishId.isEmpty() || !FishPopulationSavedData.get(serverLevel).catchFish(chunkPos, activeFishId)) {
            return false;
        }

        FishData fishData = FishDataRegistry.get(activeFishId);
        if (fishData == null) {
            return false;
        }
        Item item = FishDataRegistry.resolveItem(fishData.itemId());
        if (item == null) {
            return false;
        }

        int fishWeight = Math.max(1, Math.round(activeFishWeight * (1.0F + weightModifier / 100.0F)));
        ItemStack catchStack = FishStackUtil.createCaughtFishStack(item, fishData, catchCountForModifier(), fishWeight, level().getGameTime());
        spawnPulledItem(player, catchStack);
        rollTreasure(player, serverLevel, rodStack);
        player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, random.nextInt(6) + 1));
        if (catchStack.is(ItemTags.FISHES)) {
            player.awardStat(Stats.FISH_CAUGHT, 1);
        }
        return true;
    }

    private void rollTreasure(Player player, ServerLevel serverLevel, ItemStack rodStack) {
        int treasureChance = Mth.clamp(BASE_TREASURE_CHANCE + treasureModifier, 0, 100);
        if (random.nextInt(100) >= treasureChance) {
            return;
        }

        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, position())
                .withParameter(LootContextParams.TOOL, rodStack)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withLuck(EnchantmentHelper.getFishingLuckBonus(rodStack) + player.getLuck())
                .create(LootContextParamSets.FISHING);
        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(FMB_COMBINED_LOOT);
        List<ItemStack> treasureStacks = lootTable.getRandomItems(lootParams);
        if (treasureStacks.isEmpty()) {
            return;
        }

        for (ItemStack treasureStack : treasureStacks) {
            spawnPulledItem(player, treasureStack);
        }
        player.displayClientMessage(Component.translatable("notif.fishingmadebetter.treasure"), true);
    }

    private void spawnPulledItem(Player player, ItemStack stack) {
        double spawnY = getY();
        if (hookLiquid == HookLiquid.LAVA) {
            BlockPos pos = blockPosition();
            float lavaHeight = getTargetLiquidHeight(pos);
            if (lavaHeight > 0.0F) {
                // A caught item spawned inside lava immediately gets lava item
                // physics/fire handling before it can complete the normal fishing
                // pull. Start it just above the surface, then use the exact vanilla/
                // 1.12.2 pull vector below.
                spawnY = Math.max(spawnY, pos.getY() + lavaHeight + 0.125D);
            }
        }

        ItemEntity itemEntity = new ItemEntity(level(), getX(), spawnY, getZ(), stack);
        double x = player.getX() - itemEntity.getX();
        double y = player.getY() - itemEntity.getY();
        double z = player.getZ() - itemEntity.getZ();
        itemEntity.setDeltaMovement(x * 0.1D, y * 0.1D + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08D, z * 0.1D);
        level().addFreshEntity(itemEntity);
    }

    private int catchCountForModifier() {
        return weightModifier > 0 && random.nextInt(100) < weightModifier ? 2 : 1;
    }

    private boolean canCatchFish(FishData fishData) {
        FishingLiquid liquid = fishData.liquid();
        return FishDataRegistry.resolveItem(fishData.itemId()) != null
                && level() instanceof ServerLevel serverLevel
                && fishData.canExistAt(serverLevel, blockPosition())
                // 1.12.2 did not select fish whose minimum depth exceeded the
                // equipped reel's range.
                && fishData.minDeepLevel() <= reelRange
                && hasEnoughTargetLiquid(blockPosition())
                && (baitId.isEmpty() || BaitUtil.isValidBaitIdForFish(baitId, fishData))
                && (liquid == FishingLiquid.ANY
                || (hookLiquid == HookLiquid.WATER && liquid == FishingLiquid.WATER)
                || (hookLiquid == HookLiquid.LAVA && liquid == FishingLiquid.LAVA)
                || (hookLiquid == HookLiquid.VOID && liquid == FishingLiquid.VOID));
    }

    private boolean hasEnoughTargetLiquid(BlockPos origin) {
        if (hookLiquid == HookLiquid.VOID) {
            return true;
        }

        int matchingLiquid = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -3, -2), origin.offset(2, 0, 2))) {
            FluidState fluidState = level().getFluidState(pos);
            if ((hookLiquid == HookLiquid.WATER && fluidState.is(FluidTags.WATER))
                    || (hookLiquid == HookLiquid.LAVA && fluidState.is(FluidTags.LAVA))) {
                matchingLiquid++;
                if (matchingLiquid >= 25) {
                    return true;
                }
            }
        }
        return false;
    }

    private int catchWeight(FishData fishData) {
        int weight = Math.max(1, fishData.rarity());
        if (!baitId.isEmpty() && BaitUtil.isValidBaitIdForFish(baitId, fishData)) {
            weight += Mth.nextInt(random, 25, 75);
        }
        return weight;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!level().isClientSide) {
            hookedIn = hitResult.getEntity();
            state = State.HOOKED_IN_ENTITY;
        }
    }

    public enum HookLiquid {
        WATER,
        LAVA,
        VOID
    }

    private enum State {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING
    }
}
