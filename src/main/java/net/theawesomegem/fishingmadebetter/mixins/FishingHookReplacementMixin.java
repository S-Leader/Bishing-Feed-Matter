package net.theawesomegem.fishingmadebetter.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import net.theawesomegem.fishingmadebetter.common.entity.FmbFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WaterFishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes fishing hooks from vanilla and other mods through the reeling minigame.
 */
@Mixin(ServerLevel.class)
public abstract class FishingHookReplacementMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void fishingmadebetter$replaceExternalFishingHook(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof FishingHook hook) || hook instanceof FmbFishingHook) {
            return;
        }

        Player player = hook.getPlayerOwner();
        if (player == null) {
            return;
        }

        ItemStack rodStack = findHeldRod(player);
        if (rodStack.isEmpty()) {
            return;
        }

        ServerLevel level = (ServerLevel) (Object) this;
        cir.setReturnValue(level.addFreshEntity(new WaterFishingHook(player, level, rodStack)));
    }

    private static ItemStack findHeldRod(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.canPerformAction(ToolActions.FISHING_ROD_CAST)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        return offHand.canPerformAction(ToolActions.FISHING_ROD_CAST) ? offHand : ItemStack.EMPTY;
    }
}
