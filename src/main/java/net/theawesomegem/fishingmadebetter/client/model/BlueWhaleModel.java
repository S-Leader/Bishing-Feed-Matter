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

/**
 * Large blue whale model exported from Blockbench. All movement is driven by
 * {@link BlueWhaleAnimations}; setupAnim no longer hand-edits model bones.
 */
public final class BlueWhaleModel extends HierarchicalModel<BlueWhaleEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "blue_whale"), "main");

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

    public BlueWhaleModel(ModelPart root) {
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

        PartDefinition headTop = controller.addOrReplaceChild("Head Top", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, -80.0F));
        headTop.addOrReplaceChild("jaw_r1", CubeListBuilder.create().texOffs(456, 204).addBox(-64.0F, -60.0F, -40.0F, 120.0F, 32.0F, 108.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.0F, -4.0F, -0.1309F, 0.0F, 0.0F));
        headTop.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 440).addBox(-68.0F, -60.0F, -44.0F, 128.0F, 32.0F, 132.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, -4.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition mouth = controller.addOrReplaceChild("Mouth", CubeListBuilder.create(), PartPose.offset(0.0F, -36.0F, -6.0F));
        mouth.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-68.0F, -52.0F, -44.0F, 128.0F, 80.0F, 124.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 59.0F, -72.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition mainBody = controller.addOrReplaceChild("Main Body", CubeListBuilder.create().texOffs(0, 204).addBox(-60.0F, -72.0F, -28.0F, 112.0F, 120.0F, 116.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = mainBody.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(504, 0).addBox(-44.0F, -55.0F, 0.0F, 88.0F, 112.0F, 52.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -13.0F, 88.0F));

        PartDefinition tail3 = tail.addOrReplaceChild("Segment_3_Tail2", CubeListBuilder.create().texOffs(520, 408).addBox(-36.0F, -46.0F, -14.0F, 72.0F, 88.0F, 44.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 66.0F));

        PartDefinition tail4 = tail3.addOrReplaceChild("Segment 4 Tail", CubeListBuilder.create().texOffs(448, 700).addBox(0.0F, -52.0F, 12.0F, 0.0F, 24.0F, 32.0F, new CubeDeformation(0.0F)).texOffs(520, 540).addBox(-28.0F, -28.0F, 0.0F, 56.0F, 56.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -18.0F, 30.0F));

        PartDefinition tail5 = tail4.addOrReplaceChild("Segment 5 Tail", CubeListBuilder.create().texOffs(0, 668).addBox(-24.0F, -22.0F, 0.0F, 48.0F, 44.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 40.0F));

        PartDefinition tail6 = tail5.addOrReplaceChild("Segment 6 Tail", CubeListBuilder.create().texOffs(168, 668).addBox(-20.0F, -18.0F, 0.0F, 40.0F, 36.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 36.0F));

        PartDefinition tail7 = tail6.addOrReplaceChild("Segment 7 Tail", CubeListBuilder.create().texOffs(320, 700).addBox(-16.0F, -14.0F, 0.0F, 32.0F, 28.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 36.0F));

        tail7.addOrReplaceChild("End Tail", CubeListBuilder.create().texOffs(456, 344).addBox(-56.7678F, -1.0F, 3.9202F, 112.0F, 0.0F, 64.0F, new CubeDeformation(0.0F)).texOffs(504, 164).addBox(-12.0F, -8.0F, 0.0F, 24.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 32.0F));

        PartDefinition leftFin = mainBody.addOrReplaceChild("Left Fin", CubeListBuilder.create().texOffs(624, 636).addBox(-40.0F, 0.0F, -32.0F, 40.0F, 0.0F, 64.0F, new CubeDeformation(0.0F)), PartPose.offset(-60.0F, -20.0F, 71.0F));
        leftFin.addOrReplaceChild("Outer Left Fin", CubeListBuilder.create().texOffs(416, 636).addBox(-40.0F, 0.0F, -32.0F, 40.0F, 0.0F, 64.0F, new CubeDeformation(0.0F)), PartPose.offset(-40.0F, 0.0F, 0.0F));

        PartDefinition rightFin = mainBody.addOrReplaceChild("Right Fin", CubeListBuilder.create().texOffs(0, 604).addBox(0.0F, 0.0F, -32.0F, 40.0F, 0.0F, 64.0F, new CubeDeformation(0.0F)), PartPose.offset(52.0F, -20.0F, 72.0F));
        rightFin.addOrReplaceChild("Outer Right Fin", CubeListBuilder.create().texOffs(208, 604).addBox(0.0F, 0.0F, -32.0F, 40.0F, 0.0F, 64.0F, new CubeDeformation(0.0F)), PartPose.offset(40.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 1024, 1024);
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
     * Bounds below are the actual 1024-space model bounds from the supplied Blockbench model.
     */
    public void translateToStuckProjectile(PoseStack poseStack, int partIndex, float x, float y, float z) {
        controller.translateAndRotate(poseStack);
        switch (partIndex) {
            case 0 -> {
                if (y >= 0.0F) {
                    headTop.translateAndRotate(poseStack);
                    headUpper.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y * 2.0F - 1.0F, z, -68.0F, 60.0F, -60.0F, -28.0F, -44.0F, 88.0F);
                } else {
                    mouth.translateAndRotate(poseStack);
                    mouthCube.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y * 2.0F + 1.0F, z, -68.0F, 60.0F, -52.0F, 28.0F, -44.0F, 80.0F);
                }
            }
            case 2 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z, -44.0F, 44.0F, -55.0F, 57.0F, 0.0F, 52.0F);
            }
            case 3 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                tail3.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z, -36.0F, 36.0F, -46.0F, 42.0F, -14.0F, 30.0F);
            }
            case 4 -> {
                mainBody.translateAndRotate(poseStack);
                tail.translateAndRotate(poseStack);
                tail3.translateAndRotate(poseStack);
                tail4.translateAndRotate(poseStack);
                tail5.translateAndRotate(poseStack);
                if (z >= 0.0F) {
                    translateNormalized(poseStack, x, y, z * 2.0F - 1.0F, -24.0F, 24.0F, -22.0F, 22.0F, 0.0F, 36.0F);
                } else {
                    tail6.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x, y, z * 2.0F + 1.0F, -20.0F, 20.0F, -18.0F, 18.0F, 0.0F, 36.0F);
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
                translateNormalized(poseStack, x, y, z, -56.7678F, 55.2322F, -8.0F, 8.0F, 0.0F, 67.9202F);
            }
            case 6 -> {
                mainBody.translateAndRotate(poseStack);
                leftFin.translateAndRotate(poseStack);
                if (x < 0.0F) {
                    leftFinOuter.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x * 2.0F + 1.0F, y, z, -40.0F, 0.0F, -2.0F, 2.0F, -32.0F, 32.0F);
                } else {
                    translateNormalized(poseStack, x * 2.0F - 1.0F, y, z, -40.0F, 0.0F, -2.0F, 2.0F, -32.0F, 32.0F);
                }
            }
            case 7 -> {
                mainBody.translateAndRotate(poseStack);
                rightFin.translateAndRotate(poseStack);
                if (x > 0.0F) {
                    rightFinOuter.translateAndRotate(poseStack);
                    translateNormalized(poseStack, x * 2.0F - 1.0F, y, z, 0.0F, 40.0F, -2.0F, 2.0F, -32.0F, 32.0F);
                } else {
                    translateNormalized(poseStack, x * 2.0F + 1.0F, y, z, 0.0F, 40.0F, -2.0F, 2.0F, -32.0F, 32.0F);
                }
            }
            default -> {
                mainBody.translateAndRotate(poseStack);
                translateNormalized(poseStack, x, y, z, -60.0F, 52.0F, -72.0F, 48.0F, -28.0F, 88.0F);
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
    public void setupAnim(BlueWhaleEntity whale, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        animate(whale.beachedAnimationState, BlueWhaleAnimations.idle1, ageInTicks);
        animate(whale.swimAnimationState, BlueWhaleAnimations.swim, ageInTicks);
        animate(whale.ramAnimationState, BlueWhaleAnimations.ram, ageInTicks);
        animate(whale.blowAnimationState, BlueWhaleAnimations.blow, ageInTicks);
    }
}
