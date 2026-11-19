package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.ElectricEelItemModel;
import net.theawesomegem.fishingmadebetter.client.renderer.WhipRenderState;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectricEelWhip;
import org.joml.Vector4f;

public class EelWhipRenderer extends EntityRenderer<ElectricEelWhip, WhipRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/tentacle_spike.png");
    private static final Identifier GLOW =
            Identifier.fromNamespaceAndPath(
                    Constants.MOD_ID, "textures/entity/tentacle_spike_glow.png");
    private final ElectricEelItemModel model;

    public EelWhipRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new ElectricEelItemModel(context.bakeLayer(ElectricEelItemModel.LAYER_LOCATION));
    }

    @Override
    protected AABB getBoundingBoxForCulling(ElectricEelWhip whip) {
        return super.getBoundingBoxForCulling(whip).inflate(12);
    }

    @Override
    public WhipRenderState createRenderState() {
        return new WhipRenderState();
    }

    @Override
    public void extractRenderState(ElectricEelWhip whip, WhipRenderState state, float partialTick) {
        super.extractRenderState(whip, state, partialTick);
        Player player = whip.getOwner();
        state.hasOwner = player != null;
        state.progress = Mth.clamp(whip.getAnimationTicks(partialTick) / whip.getLifespan(), 0, 1);
        state.foil = whip.isFoil();
        if (player != null) {
            state.firstPerson =
                    player == Minecraft.getInstance().player
                            && Minecraft.getInstance().options.getCameraType().isFirstPerson();
            state.leftHand = player.getMainArm() == HumanoidArm.LEFT;
            state.yaw = Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot);
            state.pitch = player.getViewXRot(partialTick);
            model.setupAnim(state);
            queueSparks(whip, state, partialTick);
        }
    }

    @Override
    public void submit(
            WhipRenderState state,
            PoseStack pose,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (!state.hasOwner) return;
        pose.pushPose();
        transformWhip(state, pose);
        collector.submitModel(
                model,
                state,
                pose,
                model.renderType(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);
        if (state.foil)
            collector
                    .order(1)
                    .submitModel(
                            model,
                            state,
                            pose,
                            RenderTypes.entityGlint(),
                            state.lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            state.outlineColor,
                            null);
        collector
                .order(2)
                .submitModel(
                        model,
                        state,
                        pose,
                        RenderTypes.eyes(GLOW),
                        15728880,
                        OverlayTexture.NO_OVERLAY,
                        state.outlineColor,
                        null);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private void transformWhip(WhipRenderState state, PoseStack pose) {
        pose.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        pose.mulPose(Axis.XP.rotationDegrees(state.pitch));
        pose.translate(state.leftHand ? .35 : -.35, state.firstPerson ? -.25 : -.65, -.35);
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.scale(-1, -1, 1);
        pose.translate(0, -1.5, 0);
    }

    private void queueSparks(ElectricEelWhip whip, WhipRenderState state, float partialTick) {
        float midpoint = whip.getLifespan() / 2.0F;
        if (whip.tickCount < midpoint || whip.tickCount >= whip.getLifespan()) {
            return;
        }
        float progress = (whip.tickCount - midpoint) / (whip.getLifespan() - midpoint);
        float probability = Mth.cos(progress * Mth.HALF_PI);
        if (whip.getRandom().nextFloat() >= probability) {
            return;
        }
        int segment = whip.getRandom().nextInt(14) + 1;
        PoseStack worldPose = new PoseStack();
        worldPose.translate(
                Mth.lerp(partialTick, whip.xo, whip.getX()),
                Mth.lerp(partialTick, whip.yo, whip.getY()),
                Mth.lerp(partialTick, whip.zo, whip.getZ()));
        transformWhip(state, worldPose);
        model.translateToSegment(worldPose, segment);
        Vector4f position = new Vector4f(0, 0, 0, 1).mul(worldPose.last().pose());
        whip.queueSparkPosition(new Vec3(position.x(), position.y(), position.z()));
    }
}
