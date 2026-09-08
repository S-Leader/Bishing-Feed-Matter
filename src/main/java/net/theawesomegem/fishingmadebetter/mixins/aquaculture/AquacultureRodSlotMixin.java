package net.theawesomegem.fishingmadebetter.mixins.aquaculture;

import net.minecraft.world.item.ItemStack;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.teammetallurgy.aquaculture.inventory.container.slot.SlotFishingRod")
abstract class AquacultureRodSlotMixin {
    @Inject(method = {"mayPlace", "m_5857_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void fishingmadebetter$acceptFishingEvolvedRod(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(stack.getItem() instanceof BetterFishingRodItem);
    }
}
