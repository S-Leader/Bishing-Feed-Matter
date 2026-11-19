package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.FishModel;
import net.theawesomegem.fishingmadebetter.client.model.fish.PufferfishBombMidModel;
import net.theawesomegem.fishingmadebetter.client.model.fish.PufferfishBombSmallModel;
import net.theawesomegem.fishingmadebetter.client.renderer.FishRenderState;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PortedFish;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PufferfishBomb;

import java.util.Locale;

public class PortedFishRenderer extends MobRenderer<PortedFish, FishRenderState, FishModel> {
    private final String name;
    private final FishModel small;
    private final FishModel mid;
    private final FishModel big;

    public PortedFishRenderer(
            EntityRendererProvider.Context context, String name, FishModel model, boolean glow) {
        super(context, model, model.shadowRadius());
        this.name = name;
        big = model;
        small = name.equals("pufferfish_bomb") ? new PufferfishBombSmallModel(context) : model;
        mid = name.equals("pufferfish_bomb") ? new PufferfishBombMidModel(context) : model;
        if (glow) {
            addLayer(
                    new EyesLayer<>(this) {
                        @Override
                        public RenderType renderType() {
                            return RenderTypes.eyes(texture(name + "_eyes"));
                        }
                    });
        }
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(
                Constants.MOD_ID, "textures/entity/fish/" + name + ".png");
    }

    @Override
    public FishRenderState createRenderState() {
        return new FishRenderState();
    }

    @Override
    public void extractRenderState(PortedFish fish, FishRenderState state, float partialTick) {
        super.extractRenderState(fish, state, partialTick);
        state.patrick =
                fish.isStarfish()
                        && fish.hasCustomName()
                        && fish.getName()
                        .getString()
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .equals("patrick star");
        state.puffState = fish instanceof PufferfishBomb bomb ? bomb.getPuffState() : -1;
        state.fuse = fish instanceof PufferfishBomb bomb ? bomb.getFuse() - partialTick : 0;
    }

    @Override
    public Identifier getTextureLocation(FishRenderState state) {
        return texture(state.patrick ? "patrick_star" : name);
    }

    @Override
    public void submit(
            FishRenderState state,
            PoseStack pose,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        model = state.puffState == 0 ? small : state.puffState == 1 ? mid : big;
        pose.pushPose();
        if (state.puffState == PufferfishBomb.STATE_BOOM) {
            float swelling = Mth.clamp(1 - (state.fuse + 1) / 10, 0, 1);
            float scale = 1 + swelling * swelling * swelling * swelling * .3F;
            pose.scale(scale, scale, scale);
        }
        super.submit(state, pose, collector, camera);
        pose.popPose();
    }

    @Override
    protected float getShadowRadius(FishRenderState state) {
        return state.puffState < 0 ? super.getShadowRadius(state) : .1F + state.puffState * .1F;
    }

    @Override
    protected void setupRotations(
            FishRenderState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        pose.mulPose(Axis.XP.rotationDegrees(state.xRot * (float) model.xTiltScale()));
        if (!state.isInWater && model.flipInAir()) pose.mulPose(Axis.ZP.rotationDegrees(90));
    }

    @Override
    protected float getWhiteOverlayProgress(FishRenderState state) {
        return state.puffState == PufferfishBomb.STATE_BOOM && (int) state.fuse % 5 == 0 ? 1 : 0;
    }
}
