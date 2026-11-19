package net.theawesomegem.fishingmadebetter.mixins.aquaculture;

import com.teammetallurgy.aquaculture.init.AquaDataComponents;
import com.teammetallurgy.aquaculture.inventory.container.slot.SlotFishingRod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.teammetallurgy.aquaculture.inventory.container.FishingRodContainerWrapper")
abstract class AquacultureRodContainerMixin {
    @Shadow
    @Final
    private SlotFishingRod rodSlot;

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void acceptAttachments(
            int slot, ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (rodSlot.getRod().getItem() instanceof BetterFishingRodItem)
            callback.setReturnValue(
                    switch (slot) {
                        case 0 -> stack.getItem() instanceof HookItem;
                        case 1 -> BaitUtil.isPotentialBait(stack);
                        case 2 -> stack.getItem() instanceof ReelItem;
                        case 3 -> stack.getItem() instanceof BobberItem;
                        default -> false;
                    });
    }

    @Inject(method = "setItem", at = @At("RETURN"), remap = false)
    private void syncAttachments(int slot, ItemStack stack, CallbackInfo callback) {
        ItemStack rod = rodSlot.getRod();
        if (rod.getItem() instanceof BetterFishingRodItem) {
            rod.set(DataComponents.CONTAINER, rod.get(AquaDataComponents.ROD_INVENTORY.get()));
            rodSlot.set(rod);
        }
    }
}
