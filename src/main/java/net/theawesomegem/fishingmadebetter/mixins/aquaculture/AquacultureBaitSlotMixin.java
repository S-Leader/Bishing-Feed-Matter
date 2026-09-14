package net.theawesomegem.fishingmadebetter.mixins.aquaculture;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.teammetallurgy.aquaculture.inventory.container.TackleBoxContainer$2")
abstract class AquacultureBaitSlotMixin {
    @Inject(method = {"mayPlace", "m_5857_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void fishingmadebetter$useBait(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (BaitUtil.isPotentialBait(stack) && ((Slot) (Object) this).isActive()) {
            cir.setReturnValue(true);
        }
    }
}
