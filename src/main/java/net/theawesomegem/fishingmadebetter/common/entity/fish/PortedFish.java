package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

public class PortedFish extends AbstractFish {
    public PortedFish(EntityType<? extends PortedFish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes(double health, double speed) {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed);
    }

    public boolean isStarfish() {
        return getType() == ModEntities.PINK_STARFISH.get();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(
                BuiltInRegistries.ITEM.getValue(
                        BuiltInRegistries.ENTITY_TYPE.getKey(getType()).withSuffix("_bucket")));
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    public void aiStep() {
        boolean grounded = onGround();
        if (isStarfish()) setOnGround(false);
        super.aiStep();
        if (isStarfish()) setOnGround(grounded);
    }

    @Override
    protected void registerGoals() {
        if (!isStarfish()) super.registerGoals();
    }

    @Override
    public void travel(Vec3 input) {
        if (isStarfish() && isInWater()) {
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.8).add(0, -0.005, 0));
        } else {
            super.travel(input);
        }
    }

    @Override
    public void saveToBucketTag(ItemStack stack) {
        super.saveToBucketTag(stack);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registryAccess());
        addAdditionalSaveData(output);
        stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(output.buildResult()));
        stack.copyFrom(DataComponents.CUSTOM_NAME, this);
    }

    @Override
    public void loadFromBucketTag(CompoundTag tag) {
        super.loadFromBucketTag(tag);
        readAdditionalSaveData(
                TagValueInput.create(ProblemReporter.DISCARDING, registryAccess(), tag));
    }

    public static boolean checkSpawn(
            EntityType<? extends PortedFish> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return true;
        }
        if (type == ModEntities.ELECTRIC_EEL.get() || type == ModEntities.PUFFERFISH_BOMB.get()) {
            return false;
        }
        if (level.getLevel().dimension() != Level.OVERWORLD
                || pos.getY() < -20
                || pos.getY() > 40
                || !level.getFluidState(pos).is(FluidTags.WATER)
                || !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            return false;
        }
        return level.getBiome(pos).is(BiomeTags.IS_OCEAN);
    }
}
