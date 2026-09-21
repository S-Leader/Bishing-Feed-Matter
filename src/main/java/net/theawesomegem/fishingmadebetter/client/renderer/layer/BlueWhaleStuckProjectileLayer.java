package net.theawesomegem.fishingmadebetter.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.theawesomegem.fishingmadebetter.client.model.BlueWhaleModel;
import net.theawesomegem.fishingmadebetter.common.entity.BlueWhaleEntity;

import java.util.ArrayList;
import java.util.List;

public final class BlueWhaleStuckProjectileLayer extends RenderLayer<BlueWhaleEntity, BlueWhaleModel> {
    private final EntityRenderDispatcher dispatcher;
    // 只拿来当渲染模板的空壳实体，按世界缓存复用：每帧每根箭新建一个实体会白白喂饱 GC。
    private Arrow templateArrow;

    public BlueWhaleStuckProjectileLayer(EntityRendererProvider.Context context,
                                         LivingEntityRenderer<BlueWhaleEntity, BlueWhaleModel> renderer) {
        super(renderer);
        dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, BlueWhaleEntity whale,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ListTag entries = whale.getStuckProjectileData();
        if (entries.isEmpty()) {
            return;
        }

        List<CompoundTag> arrows = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            // Old saves may still contain tridents captured by earlier versions. They are
            // deliberately ignored here so they can never be rendered as ordinary arrows.
            if (!entry.getBoolean("Trident")) {
                arrows.add(entry);
            }
        }

        int arrowCount = Math.min(16, Math.min(whale.getArrowCount(), arrows.size()));
        for (int i = arrows.size() - arrowCount; i < arrows.size(); i++) {
            renderOne(poseStack, buffer, packedLight, whale, templateArrow(whale), arrows.get(i), partialTick);
        }
    }

    private Arrow templateArrow(BlueWhaleEntity whale) {
        if (templateArrow == null || templateArrow.level() != whale.level()) {
            templateArrow = new Arrow(whale.level(), whale.getX(), whale.getY(), whale.getZ());
        }
        return templateArrow;
    }

    private void renderOne(PoseStack poseStack, MultiBufferSource buffer, int packedLight, BlueWhaleEntity whale,
                           Entity projectile, CompoundTag entry, float partialTick) {
        poseStack.pushPose();

        int part = Mth.clamp(entry.getByte("Part"), 0, 7);
        float x = entry.getFloat("X");
        float y = entry.getFloat("Y");
        float z = entry.getFloat("Z");
        getParentModel().translateToStuckProjectile(poseStack, part, x, y, z);

        // The stored direction is relative to the whale when the projectile hit it.
        // Model-space Z points toward the tail and model-space Y points downward,
        // hence the sign flips for DY/DZ here.
        float directionX = entry.getFloat("DX");
        float directionY = -entry.getFloat("DY");
        float directionZ = -entry.getFloat("DZ");
        float horizontal = Mth.sqrt(directionX * directionX + directionZ * directionZ);
        projectile.setYRot((float) (Mth.atan2(directionX, directionZ) * Mth.RAD_TO_DEG));
        projectile.setXRot((float) (Mth.atan2(directionY, horizontal) * Mth.RAD_TO_DEG));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();

        dispatcher.render(projectile, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
