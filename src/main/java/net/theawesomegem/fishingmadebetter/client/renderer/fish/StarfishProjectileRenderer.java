package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PinkStarfishProjectile;

public class StarfishProjectileRenderer extends EntityRenderer<PinkStarfishProjectile> {
    private final EntityRendererProvider.Context context;

    public StarfishProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void render(PinkStarfishProjectile fish, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(fish.getRenderRotation(partialTick));
        pose.scale(0.5F, 0.5F, 0.5F);
        context.getItemRenderer().renderStatic(fish.getItem(), ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, fish.level(), fish.getId());
        pose.popPose();
        super.render(fish, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(PinkStarfishProjectile fish) {
        return new ResourceLocation(Constants.MOD_ID, "textures/item/pink_starfish.png");
    }
}
