package net.theawesomegem.fishingmadebetter.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.BlueWhaleModel;
import net.theawesomegem.fishingmadebetter.client.renderer.layer.BlueWhaleStuckProjectileLayer;
import net.theawesomegem.fishingmadebetter.common.entity.BlueWhaleEntity;

public final class BlueWhaleRenderer extends MobRenderer<BlueWhaleEntity, BlueWhaleModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/blue_whale.png");

    public BlueWhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new BlueWhaleModel(context.bakeLayer(BlueWhaleModel.LAYER_LOCATION)), 8.0F);
        addLayer(new BlueWhaleStuckProjectileLayer(context, this));
    }

    @Override
    protected void setupRotations(BlueWhaleEntity whale, PoseStack poseStack, float ageInTicks,
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
    public ResourceLocation getTextureLocation(BlueWhaleEntity whale) {
        return TEXTURE;
    }
}
