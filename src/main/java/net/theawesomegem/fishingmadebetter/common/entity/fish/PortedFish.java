package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

public class PortedFish extends AbstractFish {
    public PortedFish(EntityType<? extends PortedFish> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes(double health, double speed) {
        return createMobAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, speed);
    }

    public boolean isStarfish() {
        return getType() == ModEntities.PINK_STARFISH.get();
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(ForgeRegistries.ENTITY_TYPES.getKey(getType()).withSuffix("_bucket")));
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
        CompoundTag data = new CompoundTag();
        addAdditionalSaveData(data);
        if (hasCustomName()) data.putString("CustomName", Component.Serializer.toJson(getCustomName()));
        stack.getOrCreateTag().put("FishData", data);
    }

    @Override
    public void loadFromBucketTag(CompoundTag tag) {
        super.loadFromBucketTag(tag);
        if (tag.contains("FishData", 10)) {
            readAdditionalSaveData(tag.getCompound("FishData"));
            if (tag.getCompound("FishData").contains("CustomName", 8)) {
                setCustomName(Component.Serializer.fromJson(tag.getCompound("FishData").getString("CustomName")));
            }
        }
    }

    public static boolean checkSpawn(EntityType<? extends PortedFish> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return true;
        }
        if (type == ModEntities.ELECTRIC_EEL.get() || type == ModEntities.PUFFERFISH_BOMB.get()) {
            return false;
        }
        if (level.getLevel().dimension() != Level.OVERWORLD || pos.getY() < -20 || pos.getY() > 40
                || !level.getFluidState(pos).is(FluidTags.WATER)
                || !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            return false;
        }
        return level.getBiome(pos).is(BiomeTags.IS_OCEAN);
    }
}
