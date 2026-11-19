package net.theawesomegem.fishingmadebetter.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
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

@Mixin(ItemRenderer.class)
public abstract class EelWhipItemMixin {
    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("HEAD"), cancellable = true)
    private void hideEelWhileWhipping(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose, MultiBufferSource buffers, Level level, int light, int overlay, int seed, CallbackInfo callback) {
        if (entity instanceof Player player && stack.is(ModItems.ELECTRIC_EEL_ITEM.get())
                && leftHand == (player.getMainArm() == HumanoidArm.LEFT)
                && !level.getEntitiesOfClass(ElectricEelWhip.class, player.getBoundingBox().inflate(2), whip -> whip.getOwner() == player).isEmpty()) {
            callback.cancel();
        }
    }
}
