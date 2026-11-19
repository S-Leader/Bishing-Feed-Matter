package net.theawesomegem.fishingmadebetter.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.WhaleModel;
import net.theawesomegem.fishingmadebetter.common.entity.WhaleEntity;

public final class WhaleRenderer extends MobRenderer<WhaleEntity, WhaleRenderState, WhaleModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/whale.png");

    public WhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new WhaleModel(context.bakeLayer(WhaleModel.LAYER_LOCATION)), 8);
    }

    @Override
    public WhaleRenderState createRenderState() {
        return new WhaleRenderState();
    }

    @Override
    public void extractRenderState(WhaleEntity whale, WhaleRenderState state, float partialTick) {
        super.extractRenderState(whale, state, partialTick);
        state.beached = whale.isBeached();
        state.visualPitch = whale.getVisualPitch(partialTick);
        state.beachedAnimationState.copyFrom(whale.beachedAnimationState);
        state.swimAnimationState.copyFrom(whale.swimAnimationState);
        state.ramAnimationState.copyFrom(whale.ramAnimationState);
        state.blowAnimationState.copyFrom(whale.blowAnimationState);
    }

    @Override
    protected AABB getBoundingBoxForCulling(WhaleEntity whale) {
        return super.getBoundingBoxForCulling(whale).inflate(35);
    }

    @Override
    protected void setupRotations(
            WhaleRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        if (!state.beached) {
            pose.translate(0, 3.75, 0);
            pose.mulPose(Axis.XP.rotationDegrees(-state.visualPitch));
            pose.translate(0, -3.75, 0);
        }
    }

    @Override
    public Identifier getTextureLocation(WhaleRenderState state) {
        return TEXTURE;
    }
}
