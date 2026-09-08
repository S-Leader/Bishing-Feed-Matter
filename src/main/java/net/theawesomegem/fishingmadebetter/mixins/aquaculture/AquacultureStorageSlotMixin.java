package net.theawesomegem.fishingmadebetter.mixins.aquaculture;

import net.minecraft.world.item.ItemStack;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.teammetallurgy.aquaculture.inventory.container.TackleBoxContainer$5")
abstract class AquacultureStorageSlotMixin {
    @Inject(method = {"mayPlace", "m_5857_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void fishingmadebetter$storeGear(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof BetterFishingRodItem || stack.getItem() instanceof HookItem
                || stack.getItem() instanceof ReelItem || stack.getItem() instanceof BobberItem
                || BaitUtil.isPotentialBait(stack)) {
            cir.setReturnValue(true);
        }
    }
}
