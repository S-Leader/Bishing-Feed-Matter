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
import net.theawesomegem.fishingmadebetter.common.entity.WhaleEntity;

public final class WhaleModel extends HierarchicalModel<WhaleEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "whale"), "main");

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

    public WhaleModel(ModelPart root) {
        this.root = root;
        this.controller = root.getChild("Controller");
        this.headTop = controller.getChild("Head Top");
        this.headUpper = headTop.getChild("head_r1");
        this.mouth = controller.getChild("Mouth");
        this.mouthCube = mouth.getChild("cube_r1");
        this.mainBody = controller.getChild("Main Body");
        this.tail = mainBody.getChild("Tail");
        this.tail3 = tail.getChild("Segment_3_Tail2");
        this.tail4 = tail3.getChild("Segment 4 Tail");
        this.tail5 = tail4.getChild("Segment 5 Tail");
        this.tail6 = tail5.getChild("Segment 6 Tail");
        this.tail7 = tail6.getChild("Segment 7 Tail");
        this.tailEnd = tail7.getChild("End Tail");
        this.leftFin = mainBody.getChild("Left Fin");
        this.leftFinOuter = leftFin.getChild("Outer Left Fin");
        this.rightFin = mainBody.getChild("Right Fin");
        this.rightFinOuter = rightFin.getChild("Outer Right Fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition controller = partdefinition.addOrReplaceChild("Controller", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headTop = controller.addOrReplaceChild("Head Top", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -40.0F));
        headTop.addOrReplaceChild("jaw_r1", CubeListBuilder.create().texOffs(228, 102).addBox(-32.0F, -30.0F, -20.0F, 60.0F, 16.0F, 54.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, -2.0F, -0.1309F, 0.0F, 0.0F));
        headTop.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 220).addBox(-34.0F, -30.0F, -22.0F, 64.0F, 16.0F, 66.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, -2.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition mouth = controller.addOrReplaceChild("Mouth", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, -3.0F));
        mouth.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-34.0F, -26.0F, -22.0F, 64.0F, 40.0F, 62.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 29.5F, -36.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition mainBody = controller.addOrReplaceChild("Main Body", CubeListBuilder.create().texOffs(0, 102).addBox(-30.0F, -36.0F, -14.0F, 56.0F, 60.0F, 58.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = mainBody.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(252, 0).addBox(-22.0F, -27.5F, 0.0F, 44.0F, 56.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -6.5F, 44.0F));

        PartDefinition tail3 = tail.addOrReplaceChild("Segment_3_Tail2", CubeListBuilder.create().texOffs(260, 204).addBox(-18.0F, -23.0F, -7.0F, 36.0F, 44.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 33.0F));

        PartDefinition tail4 = tail3.addOrReplaceChild("Segment 4 Tail", CubeListBuilder.create().texOffs(224, 350).addBox(0.0F, -26.0F, 6.0F, 0.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(260, 270).addBox(-14.0F, -14.0F, 0.0F, 28.0F, 28.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 15.0F));

        PartDefinition tail5 = tail4.addOrReplaceChild("Segment 5 Tail", CubeListBuilder.create().texOffs(0, 334).addBox(-12.0F, -11.0F, 0.0F, 24.0F, 22.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 20.0F));

        PartDefinition tail6 = tail5.addOrReplaceChild("Segment 6 Tail", CubeListBuilder.create().texOffs(84, 334).addBox(-10.0F, -9.0F, 0.0F, 20.0F, 18.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 18.0F));

        PartDefinition tail7 = tail6.addOrReplaceChild("Segment 7 Tail", CubeListBuilder.create().texOffs(160, 350).addBox(-8.0F, -7.0F, 0.0F, 16.0F, 14.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 18.0F));

        tail7.addOrReplaceChild("End Tail", CubeListBuilder.create().texOffs(228, 172).addBox(-28.3839F, -0.5F, 1.9601F, 56.0F, 0.0F, 32.0F, new CubeDeformation(0.0F)).texOffs(252, 82).addBox(-6.0F, -4.0F, 0.0F, 12.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 16.0F));

        PartDefinition leftFin = mainBody.addOrReplaceChild("Left Fin", CubeListBuilder.create().texOffs(312, 318).addBox(-20.0F, 0.0F, -16.0F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(-30.0F, -10.0F, 35.5F));
        leftFin.addOrReplaceChild("Outer Left Fin", CubeListBuilder.create().texOffs(208, 318).addBox(-20.0F, 0.0F, -16.0F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.0F, 0.0F, 0.0F));

        PartDefinition rightFin = mainBody.addOrReplaceChild("Right Fin", CubeListBuilder.create().texOffs(0, 302).addBox(0.0F, 0.0F, -16.0F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(26.0F, -10.0F, 36.0F));
        rightFin.addOrReplaceChild("Outer Right Fin", CubeListBuilder.create().texOffs(104, 302).addBox(0.0F, 0.0F, -16.0F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(20.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public void translateToBody(PoseStack poseStack) {
        controller.translateAndRotate(poseStack);
        mainBody.translateAndRotate(poseStack);
    }

    /**
     * Rebuild the stored normalized projectile hit position inside the matching animated model bone.
     * Bounds below are the actual large-model pixel bounds, not the old half-size model bounds.
     */
    public void translateToStuckProjectile(PoseStack poseStack, int partIndex, float x, float y, float z) {
        controller.translateAndRotate(poseStack);
        switch (partIndex) {
            case 0 -> {
                if (y >= 0.0F) {
                    headTop.translateAndRotate(poseStack);
                    headUpper.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y * 2.0F - 1.0F, z, -34.0F, 30.0F, -30.0F, -14.0F, -22.0F, 44.0F);
                } else {
                    mouth.translateAndRotate(poseStack);
                    mouthCube.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y * 2.0F + 1.0F, z, -34.0F, 30.0F, -26.0F, 14.0F, -22.0F, 40.0F);
                }
            }
            case 2 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z, -22.0F, 22.0F, -27.5F, 28.5F, 0.0F, 26.0F);
            }
            case 3 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                tail3.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z, -18.0F, 18.0F, -23.0F, 21.0F, -7.0F, 15.0F);
            }
            case 4 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                tail3.translateAndRotate(poseStack);
                tail4.translateAndRotate(poseStack);
                tail5.translateAndRotate(poseStack);
                if (z >= 0.0F) {
                    translateNormalized(poseStack, x, y, z * 2.0F - 1.0F, -12.0F, 12.0F, -11.0F, 11.0F, 0.0F, 18.0F);
                } else {
                    tail6.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y, z * 2.0F + 1.0F, -10.0F, 10.0F, -9.0F, 9.0F, 0.0F, 18.0F);
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
                translateNormalized(poseStack, x, y, z, -28.3839F, 27.6161F, -4.0F, 4.0F, 0.0F, 33.9601F);
            }
            case 6 -> {
                mainBody.translateAndRotate(poseStack);
                leftFin.translateAndRotate(poseStack);
                if (x < 0.0F) {
                    leftFinOuter.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x * 2.0F + 1.0F, y, z, -20.0F, 0.0F, -1.0F, 1.0F, -16.0F, 16.0F);
                } else {
                    translateNormalized(poseStack, x * 2.0F - 1.0F, y, z, -20.0F, 0.0F, -1.0F, 1.0F, -16.0F, 16.0F);
                }
            }
            case 7 -> {
                mainBody.translateAndRotate(poseStack);
                rightFin.translateAndRotate(poseStack);
                if (x > 0.0F) {
                    rightFinOuter.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x * 2.0F - 1.0F, y, z, 0.0F, 20.0F, -1.0F, 1.0F, -16.0F, 16.0F);
                } else {
                    translateNormalized(poseStack, x * 2.0F + 1.0F, y, z, 0.0F, 20.0F, -1.0F, 1.0F, -16.0F, 16.0F);
                }
            }
            default -> {
                mainBody.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z, -30.0F, 26.0F, -36.0F, 24.0F, -14.0F, 44.0F);
            }
        }
    }

    private static void translateNormalized(PoseStack poseStack, float x, float y, float z, float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        float modelX = Mth.lerp((Mth.clamp(x, -1.0F, 1.0F) + 1.0F) * 0.5F, minX, maxX);
        float modelY = Mth.lerp((1.0F - Mth.clamp(y, -1.0F, 1.0F)) * 0.5F, minY, maxY);
        float modelZ = Mth.lerp((1.0F - Mth.clamp(z, -1.0F, 1.0F)) * 0.5F, minZ, maxZ);
        poseStack.translate(modelX / 16.0F, modelY / 16.0F, modelZ / 16.0F);
    }

    @Override
    public void setupAnim(WhaleEntity whale, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        animate(whale.beachedAnimationState, WhaleAnimations.idle1, ageInTicks);
        animate(whale.swimAnimationState, WhaleAnimations.swim, ageInTicks);
        animate(whale.ramAnimationState, WhaleAnimations.ram, ageInTicks);
        animate(whale.blowAnimationState, WhaleAnimations.blow, ageInTicks);
    }
}
