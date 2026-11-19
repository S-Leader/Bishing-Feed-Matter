package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class WaterIgnoringDamageCalculator extends ExplosionDamageCalculator {
    @Override
    public Optional<Float> getBlockExplosionResistance(
            Explosion explosion,
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            FluidState fluid) {
        if (state.is(Blocks.WATER)) return Optional.of(0F);
        if (fluid.is(FluidTags.WATER))
            return Optional.of(state.getBlock().getExplosionResistance());
        return super.getBlockExplosionResistance(explosion, level, pos, state, fluid);
    }

    @Override
    public boolean shouldBlockExplode(
            Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
        return !state.getFluidState().is(FluidTags.WATER)
                && super.shouldBlockExplode(explosion, level, pos, state, power);
    }
}
