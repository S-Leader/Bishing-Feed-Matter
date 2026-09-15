package net.theawesomegem.fishingmadebetter.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.BlueWhaleEntity;

public final class BlueWhaleModel extends HierarchicalModel<BlueWhaleEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "blue_whale"), "main");
    /** Model pixels; only the rendered whale moves, while the entity and multipart hitboxes stay put. */
    private static final float MODEL_RAISE = 6.0F;
    private final ModelPart root;
    private final ModelPart controller;
    private final ModelPart headTop;
    private final ModelPart headUpper;
    private final ModelPart mouth;
    private final ModelPart mouthCube;
    private final ModelPart mainBody;
    private final ModelPart tail;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail6;
    private final ModelPart tail7;
    private final ModelPart tailEnd;
    private final ModelPart leftFin;
    private final ModelPart leftFinOuter;
    private final ModelPart rightFin;
    private final ModelPart rightFinOuter;
    private final ModelPart[] tailSegments;
    /** 各尾骨骼在脊柱上离重心的距离（格），与骨骼原点 z 一一对应。 */
    private static final double[] TAIL_SEGMENT_DISTANCES = {1.375D, 2.1875D, 2.875D, 3.5D, 4.0625D, 4.625D, 5.125D};

    public BlueWhaleModel(ModelPart root) {
        this.root = root;
        this.controller = root.getChild("controller");
        this.headTop = this.controller.getChild("headTop");
        this.headUpper = this.headTop.getChild("head_r1");
        this.mouth = this.controller.getChild("mouth");
        this.mouthCube = this.mouth.getChild("cube_r1");
        this.mainBody = this.controller.getChild("mainBody");
        this.tail = this.mainBody.getChild("tail");
        this.tail3 = this.tail.getChild("tail3");
        this.tail4 = this.tail3.getChild("tail4");
        this.tail5 = this.tail4.getChild("tail5");
        this.tail6 = this.tail5.getChild("tail6");
        this.tail7 = this.tail6.getChild("tail7");
        this.tailEnd = this.tail7.getChild("tailEnd");
        this.leftFin = this.mainBody.getChild("leftFin");
        this.leftFinOuter = this.leftFin.getChild("leftFinOuter");
        this.rightFin = this.mainBody.getChild("rightFin");
        this.rightFinOuter = this.rightFin.getChild("rightFinOuter");
        this.tailSegments = new ModelPart[]{tail, tail3, tail4, tail5, tail6, tail7, tailEnd};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition controller = partdefinition.addOrReplaceChild("controller", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition headTop = controller.addOrReplaceChild("headTop", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -20.0F));

        PartDefinition jaw_r1 = headTop.addOrReplaceChild("jaw_r1", CubeListBuilder.create().texOffs(114, 51).addBox(-16.0F, -15.0F, -10.0F, 30.0F, 8.0F, 27.0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.0F, 5.5F, -1.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition head_r1 = headTop.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 110).addBox(-17.0F, -15.0F, -11.0F, 32.0F, 8.0F, 33.0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.0F, -2.5F, -1.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition mouth = controller.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, -20.0F));

        PartDefinition cube_r1 = mouth.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, -13.0F, -11.0F, 32.0F, 20.0F, 31.0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.0F, -1.25F, 0.5F, 0.0873F, 0.0F, 0.0F));

        PartDefinition mainBody = controller.addOrReplaceChild("mainBody", CubeListBuilder.create().texOffs(0, 51).addBox(-15.0F, -18.0F, -7.0F, 28.0F, 30.0F, 29.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = mainBody.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(126, 0).addBox(-12.0F, -14.0F, 0.0F, 22.0F, 28.0F, 13.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -3.0F, 22.0F));

        PartDefinition tail3 = tail.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(130, 86).addBox(-10.0F, -11.0F, 0.0F, 18.0F, 22.0F, 11.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -1.0F, 13.0F));

        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(138, 180).addBox(0.0F, -13.0F, 3.0F, 0.0F, 6.0F, 8.0F, new CubeDeformation(0F)).texOffs(130, 137).addBox(-7.0F, -7.0F, 0.0F, 14.0F, 14.0F, 10.0F, new CubeDeformation(0F)), PartPose.offset(-1.0F, -4.0F, 11.0F));

        PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(162, 161).addBox(-6.0F, -5.5F, 0.0F, 12.0F, 11.0F, 9.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -1.5F, 10.0F));

        PartDefinition tail6 = tail5.addOrReplaceChild("tail6", CubeListBuilder.create().texOffs(178, 137).addBox(-5.0F, -4.5F, 0.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -1.0F, 9.0F));

        PartDefinition tail7 = tail6.addOrReplaceChild("tail7", CubeListBuilder.create().texOffs(102, 181).addBox(-4.0F, -3.5F, 0.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -1.0F, 9.0F));

        PartDefinition tailEnd = tail7.addOrReplaceChild("tailEnd", CubeListBuilder.create().texOffs(-16, 169).addBox(-14.1919F, -0.25F, 0.9801F, 28.0F, 0.0F, 16.0F, new CubeDeformation(0F)).texOffs(58, 169).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -1.5F, 8.0F));

        PartDefinition leftFin = mainBody.addOrReplaceChild("leftFin", CubeListBuilder.create().texOffs(109, 162).addBox(-10.0F, 0.0F, -8.0F, 10.0F, 0.0F, 16.0F, new CubeDeformation(0F)), PartPose.offset(-15.0F, -5.0F, 17.75F));

        PartDefinition leftFinOuter = leftFin.addOrReplaceChild("leftFinOuter", CubeListBuilder.create().texOffs(55, 152).addBox(-10.0F, 0.0F, -8.0F, 10.0F, 0.0F, 16.0F, new CubeDeformation(0F)), PartPose.offset(-10.0F, 0.0F, 0.0F));

        PartDefinition rightFin = mainBody.addOrReplaceChild("rightFin", CubeListBuilder.create().texOffs(131, 120).addBox(0.0F, 0.0F, -8.0F, 10.0F, 0.0F, 16.0F, new CubeDeformation(0F)), PartPose.offset(13.0F, -5.0F, 18.0F));

        PartDefinition rightFinOuter = rightFin.addOrReplaceChild("rightFinOuter", CubeListBuilder.create().texOffs(1, 152).addBox(0.0F, 0.0F, -8.0F, 10.0F, 0.0F, 16.0F, new CubeDeformation(0F)), PartPose.offset(10.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    /**
     * 尾部骨骼按自身的滞后偏航角相对父骨骼旋转，与实体侧的体节碰撞箱共用同一份历史数据，
     * 保证看到的弯曲和真正能打到的判定框一致。
     */
    private void applySpineBend(BlueWhaleEntity whale, float partialTick) {
        float parentYaw = whale.getSegmentYaw(0.0D, partialTick);
        for (int i = 0; i < tailSegments.length; i++) {
            float yaw = whale.getSegmentYaw(TAIL_SEGMENT_DISTANCES[i], partialTick);
            tailSegments[i].yRot += Mth.wrapDegrees(yaw - parentYaw) * Mth.DEG_TO_RAD;
            parentYaw = yaw;
        }
    }

    private static float radians(float degrees) {
        return degrees * Mth.DEG_TO_RAD;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public void translateToBody(PoseStack poseStack) {
        controller.translateAndRotate(poseStack);
        mainBody.translateAndRotate(poseStack);
    }

    public void translateToStuckProjectile(PoseStack poseStack, int partIndex, float x, float y, float z) {
        controller.translateAndRotate(poseStack);
        switch (partIndex) {
            case 0 -> {
                if (y >= 0.0F) {
                    headTop.translateAndRotate(poseStack);
                    headUpper.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y * 2.0F - 1.0F, z,
                            -17.0F, 15.0F, -15.0F, -7.0F, -11.0F, 22.0F);
                } else {
                    mouth.translateAndRotate(poseStack);
                    mouthCube.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y * 2.0F + 1.0F, z,
                            -17.0F, 15.0F, -13.0F, 7.0F, -11.0F, 20.0F);
                }
            }
            case 2 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z,
                        -12.0F, 10.0F, -14.0F, 14.0F, 0.0F, 13.0F);
            }
            case 3 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                tail3.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z,
                        -10.0F, 8.0F, -11.0F, 11.0F, 0.0F, 11.0F);
            }
            case 4 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                tail3.translateAndRotate(poseStack);
                tail4.translateAndRotate(poseStack);
                tail5.translateAndRotate(poseStack);
                if (z >= 0.0F) {
                    translateNormalized(poseStack, x, y, z * 2.0F - 1.0F,
                            -6.0F, 6.0F, -5.5F, 5.5F, 0.0F, 9.0F);
                } else {
                    tail6.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y, z * 2.0F + 1.0F,
                            -5.0F, 5.0F, -4.5F, 4.5F, 0.0F, 9.0F);
                }
            }
            case 5 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                tail3.translateAndRotate(poseStack);
                tail4.translateAndRotate(poseStack);
                tail5.translateAndRotate(poseStack);
                tail6.translateAndRotate(poseStack);
                tail7.translateAndRotate(poseStack);
                tailEnd.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z,
                        -14.1919F, 13.8081F, -2.0F, 2.0F, 0.0F, 16.9801F);
            }
            default -> {
                mainBody.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z,
                        -15.0F, 13.0F, -18.0F, 12.0F, -7.0F, 22.0F);
            }
        }
    }

    private static void translateNormalized(PoseStack poseStack, float x, float y, float z,
                                            float minX, float maxX, float minY, float maxY,
                                            float minZ, float maxZ) {
        float modelX = Mth.lerp((Mth.clamp(x, -1.0F, 1.0F) + 1.0F) * 0.5F, minX, maxX);
        float modelY = Mth.lerp((1.0F - Mth.clamp(y, -1.0F, 1.0F)) * 0.5F, minY, maxY);
        float modelZ = Mth.lerp((1.0F - Mth.clamp(z, -1.0F, 1.0F)) * 0.5F, minZ, maxZ);
        poseStack.translate(modelX / 16.0F, modelY / 16.0F, modelZ / 16.0F);
    }

    @Override
    public void setupAnim(BlueWhaleEntity whale, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        controller.y -= MODEL_RAISE;
        float moving = Mth.clamp((float) whale.getDeltaMovement().length() * 4.0F, 0.18F, 1.0F);
        float wave = Mth.sin(ageInTicks * 0.18F);
        float partialTick = ageInTicks - whale.tickCount;
        float beached = whale.getBeachedProgress(partialTick);
        float bodyPitch = whale.getVisualPitch(partialTick) * Mth.DEG_TO_RAD * (1.0F - beached);
        // 搁浅的鲸鱼尾巴使不上劲：整条尾链的摆幅随搁浅进度归零。
        float tailAmplitude = 1.65F * (1.0F - beached);
        applySpineBend(whale, partialTick);

        // A hard ram collision leaves the whale rigid for a short time, like an axolotl playing dead.
        if (whale.isStunned()) {
            controller.xRot = bodyPitch;
            leftFin.zRot = -0.08F;
            rightFin.zRot = 0.08F;
            return;
        }

        controller.xRot = bodyPitch;
        tail.xRot += wave * 0.035F * moving * tailAmplitude;
        tail3.xRot += Mth.sin(ageInTicks * 0.18F - 0.18F) * 0.038F * moving * tailAmplitude;
        tail4.xRot += Mth.sin(ageInTicks * 0.18F - 0.36F) * 0.042F * moving * tailAmplitude;
        tail5.xRot += Mth.sin(ageInTicks * 0.18F - 0.54F) * 0.045F * moving * tailAmplitude;
        tail6.xRot += Mth.sin(ageInTicks * 0.18F - 0.72F) * 0.048F * moving * tailAmplitude;
        tail7.xRot += Mth.sin(ageInTicks * 0.18F - 0.90F) * 0.052F * moving * tailAmplitude;
        tailEnd.xRot += Mth.sin(ageInTicks * 0.18F - 1.08F) * 0.056F * moving * tailAmplitude;
        leftFin.zRot = -0.05F + wave * 0.045F;
        rightFin.zRot = 0.05F - wave * 0.045F;
        leftFinOuter.zRot = wave * 0.025F * (1.0F - beached);
        rightFinOuter.zRot = -wave * 0.025F * (1.0F - beached);

        if (whale.getAction() == BlueWhaleEntity.ACTION_BLOW) {
            float time = Mth.clamp(whale.getActionTick() / 32.0F, 0.0F, 1.0F);
            float lift = Mth.sin(time * Mth.PI);
            headTop.xRot -= lift * 0.16F;
            headTop.y -= lift * 1.8F;
            mouth.xRot += lift * 0.055F;
            controller.xRot -= lift * 0.035F;
        } else if (whale.getAction() == BlueWhaleEntity.ACTION_RAM) {
            float time = Mth.clamp(whale.getActionTick() / 24.0F, 0.0F, 1.0F);
            float drive = Mth.sin(time * Mth.PI);
            controller.z -= drive * 2.5F;
            headTop.xRot += drive * 0.09F;
            mouth.xRot -= drive * 0.06F;
            leftFin.zRot -= drive * 0.22F;
            rightFin.zRot += drive * 0.22F;
            tail.xRot -= drive * 0.12F;
        }

        if (beached > 0.0F) {
            float sigh = Mth.sin(ageInTicks * 0.07F) * beached;
            controller.y += (0.6F + sigh * 0.55F) * beached;
            mainBody.yScale = 1.0F + (0.018F + sigh * 0.012F) * beached;
            headTop.xRot += (-0.10F + sigh * 0.025F) * beached;
            mouth.xRot += (0.08F - sigh * 0.02F) * beached;
            leftFin.zRot = Mth.lerp(beached, leftFin.zRot, -0.28F);
            rightFin.zRot = Mth.lerp(beached, rightFin.zRot, 0.28F);
            tail.xRot += -0.22F * beached;
            tail3.xRot += -0.18F * beached;
            tail4.xRot += -0.15F * beached;
            tail5.xRot += -0.12F * beached;
            tail6.xRot += -0.10F * beached;
            tail7.xRot += -0.08F * beached;
            tailEnd.xRot += -0.05F * beached;
        }
    }
}
