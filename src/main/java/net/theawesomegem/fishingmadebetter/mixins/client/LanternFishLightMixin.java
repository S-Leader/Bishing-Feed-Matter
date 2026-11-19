package net.theawesomegem.fishingmadebetter.mixins.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.theawesomegem.fishingmadebetter.client.LanternFishLighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class LanternFishLightMixin {
    @Inject(
            method =
                    "getLightCoords(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true)
    private static void lanternLight(
            LevelRenderer.BrightnessGetter brightness,
            BlockAndLightGetter level,
            BlockState state,
            BlockPos pos,
            CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(LanternFishLighting.light(pos, callback.getReturnValue()));
    }
}
