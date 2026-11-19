package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.LanternFishHelmetModel;
import net.theawesomegem.fishingmadebetter.registry.ModItems;

public final class LanternFishHelmetGlowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation GLOW = new ResourceLocation(Constants.MOD_ID, "textures/entity/fish/lantern_fish_helmet_glowmask.png");
    private final LanternFishHelmetModel model = new LanternFishHelmetModel();

    public LanternFishHelmetGlowLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.LANTERN_FISH_ITEM.get())
                || !(getParentModel() instanceof HumanoidModel<?> parent)) {
            return;
        }
        model.young = parent.young;
        model.riding = parent.riding;
        model.attackTime = parent.attackTime;
        model.head.copyFrom(parent.head);
        model.hat.copyFrom(parent.hat);
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        model.renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(GLOW)), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
