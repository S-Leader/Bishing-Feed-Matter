package net.theawesomegem.fishingmadebetter.mixins.aquaculture;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
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
@Mixin(targets = "com.teammetallurgy.aquaculture.block.blockentity.TackleBoxBlockEntity$2")
abstract class AquacultureTackleBoxHandlerMixin {
    @Inject(
            method = "isValid(ILnet/neoforged/neoforge/transfer/item/ItemResource;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void fishingmadebetter$acceptGear(
            int slot, ItemResource resource, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = resource.toStack();
        if (slot == 0) {
            if (stack.getItem() instanceof BetterFishingRodItem) {
                cir.setReturnValue(true);
            }
            return;
        }
        if (stack.getItem() instanceof HookItem
                || stack.getItem() instanceof ReelItem
                || stack.getItem() instanceof BobberItem
                || BaitUtil.isPotentialBait(stack)) {
            cir.setReturnValue(true);
        }
    }
}
