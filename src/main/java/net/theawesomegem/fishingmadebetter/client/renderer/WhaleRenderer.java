package net.theawesomegem.fishingmadebetter.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.WhaleModel;
import net.theawesomegem.fishingmadebetter.common.entity.WhaleEntity;

public final class WhaleRenderer extends MobRenderer<WhaleEntity, WhaleModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/whale.png");

    public WhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new WhaleModel(context.bakeLayer(WhaleModel.LAYER_LOCATION)), 8.0F);
    }

    @Override
    protected void setupRotations(WhaleEntity whale, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick) {
        super.setupRotations(whale, poseStack, ageInTicks, rotationYaw, partialTick);
        if (!whale.isBeached()) {
            // Pitch around the middle of the 7.5-block-tall torso. Rotating around the entity's
            // feet makes this 32-block whale seesaw vertically whenever its pitch changes.
            poseStack.translate(0.0F, 3.75F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-whale.getVisualPitch(partialTick)));
            poseStack.translate(0.0F, -3.75F, 0.0F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(WhaleEntity whale) {
        return TEXTURE;
    }
}
