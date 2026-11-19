package net.theawesomegem.fishingmadebetter.mixins.client;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectricEelWhip;
import net.theawesomegem.fishingmadebetter.registry.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public abstract class EelWhipItemMixin {
    @Inject(method = "updateForTopItem", at = @At("HEAD"), cancellable = true)
    private void hideEelWhileWhipping(
            ItemStackRenderState output,
            ItemStack stack,
            ItemDisplayContext context,
            Level level,
            ItemOwner owner,
            int seed,
            CallbackInfo callback) {
        boolean hand =
                context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                        || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (hand
                && owner instanceof Player player
                && stack == player.getMainHandItem()
                && stack.is(ModItems.ELECTRIC_EEL_ITEM.get())
                && !player.level()
                .getEntitiesOfClass(
                        ElectricEelWhip.class,
                        player.getBoundingBox().inflate(2),
                        whip -> whip.getOwner() == player)
                .isEmpty()) {
            output.clear();
            callback.cancel();
        }
    }
}
