package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.FishModel;
import net.theawesomegem.fishingmadebetter.client.model.fish.PufferfishBombMidModel;
import net.theawesomegem.fishingmadebetter.client.model.fish.PufferfishBombSmallModel;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PortedFish;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PufferfishBomb;

import java.util.Locale;

public class PortedFishRenderer extends MobRenderer<PortedFish, FishModel> {
    private final String name;
    private final FishModel small;
    private final FishModel mid;
    private final FishModel big;

    public PortedFishRenderer(EntityRendererProvider.Context context, String name, FishModel model, boolean glow) {
        super(context, model, model.shadowRadius());
        this.name = name;
        big = model;
        small = name.equals("pufferfish_bomb") ? new PufferfishBombSmallModel(context) : model;
        mid = name.equals("pufferfish_bomb") ? new PufferfishBombMidModel(context) : model;
        if (glow) {
            addLayer(new EyesLayer<>(this) {
                @Override
                public RenderType renderType() {
                    return RenderType.eyes(texture(name + "_eyes"));
                }
            });
        }
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(Constants.MOD_ID, "textures/entity/fish/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(PortedFish fish) {
        if (fish.isStarfish() && fish.hasCustomName() && fish.getName().getString().trim().toLowerCase(Locale.ROOT).equals("patrick star")) {
            return texture("patrick_star");
        }
        return texture(name);
    }

    @Override
    public void render(PortedFish fish, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        if (fish instanceof PufferfishBomb bomb) {
            model = bomb.getPuffState() == 0 ? small : bomb.getPuffState() == 1 ? mid : big;
            shadowRadius = 0.1F + bomb.getPuffState() * 0.1F;
            if (bomb.getPuffState() == PufferfishBomb.STATE_BOOM) {
                float swelling = Mth.clamp(1 - (bomb.getFuse() - partialTick + 1) / 10, 0, 1);
                float scale = 1 + swelling * swelling * swelling * swelling * 0.3F;
                pose.scale(scale, scale, scale);
            }
        }
        super.render(fish, yaw, partialTick, pose, buffers, light);
        pose.popPose();
    }

    @Override
    protected void setupRotations(PortedFish fish, PoseStack pose, float age, float yaw, float partialTick) {
        super.setupRotations(fish, pose, age, yaw, partialTick);
        pose.mulPose(Axis.XP.rotationDegrees(fish.getXRot() * (float) model.xTiltScale()));
        if (!fish.isInWater() && model.flipInAir()) pose.mulPose(Axis.ZP.rotationDegrees(90));
    }

    @Override
    protected float getWhiteOverlayProgress(PortedFish fish, float partialTick) {
        return fish instanceof PufferfishBomb bomb && bomb.getPuffState() == PufferfishBomb.STATE_BOOM && bomb.getFuse() % 5 == 0 ? 1 : 0;
    }
}
