package net.theawesomegem.fishingmadebetter.mixins.aquaculture;

import net.minecraft.world.item.ItemStack;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.teammetallurgy.aquaculture.inventory.container.TackleBoxContainer$4")
abstract class AquacultureBobberSlotMixin {
    @Inject(method = {"mayPlace", "m_5857_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void fishingmadebetter$useBobber(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(stack.getItem() instanceof BobberItem);
    }
}
