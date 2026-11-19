package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.ElectricEelItemModel;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectricEelWhip;

public class EelWhipRenderer extends EntityRenderer<ElectricEelWhip> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/tentacle_spike.png");
    private static final ResourceLocation GLOW = new ResourceLocation(Constants.MOD_ID, "textures/entity/tentacle_spike_glow.png");
    private final ElectricEelItemModel<ElectricEelWhip> model;

    public EelWhipRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new ElectricEelItemModel<>(context.bakeLayer(ElectricEelItemModel.LAYER_LOCATION));
    }

    @Override
    public void render(ElectricEelWhip whip, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        Player player = whip.getOwner();
        if (player == null) return;
        pose.pushPose();
        boolean firstPerson = player == Minecraft.getInstance().player && entityRenderDispatcher.options.getCameraType().isFirstPerson();
        pose.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, player.yHeadRotO, player.yHeadRot)));
        pose.mulPose(Axis.XP.rotationDegrees(player.getViewXRot(partialTick)));
        pose.translate(player.getMainArm() == HumanoidArm.RIGHT ? -0.35 : 0.35, firstPerson ? -0.25 : -0.65, -0.35);
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.scale(-1, -1, 1);
        pose.translate(0, -1.5, 0);
        model.setupAnim(whip, 0, 0, whip.tickCount + partialTick, 0, 0);
        model.renderToBuffer(pose, ItemRenderer.getFoilBufferDirect(buffers, model.renderType(TEXTURE), false, whip.isFoil()), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        model.renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(GLOW)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ElectricEelWhip whip) {
        return TEXTURE;
    }
}
