package net.theawesomegem.fishingmadebetter.client.model;// Made with Blockbench 5.2.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.WhaleEntity;

public final class WhaleModel extends HierarchicalModel<WhaleEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "whale"), "main");

    private final ModelPart root;
    private final ModelPart controller;
    private final ModelPart headTop;
    private final ModelPart mouth;
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
        this.controller = root.getChild("controller");
        this.headTop = this.controller.getChild("headTop");
        this.mouth = this.controller.getChild("mouth");
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
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition controller = partdefinition.addOrReplaceChild("controller", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headTop = controller.addOrReplaceChild("headTop", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -40.0F));

        PartDefinition jaw_r1 = headTop.addOrReplaceChild("jaw_r1", CubeListBuilder.create().texOffs(228, 102).addBox(-32.0F, -30.0F, -20.0F, 60.0F, 16.0F, 54.0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.0F, 11.0F, -2.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition head_r1 = headTop.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 220).addBox(-34.0F, -30.0F, -22.0F, 64.0F, 16.0F, 66.0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.0F, -5.0F, -2.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition mouth = controller.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, -40.0F));

        PartDefinition cube_r1 = mouth.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-34.0F, -26.0F, -22.0F, 64.0F, 40.0F, 62.0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 1.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition mainBody = controller.addOrReplaceChild("mainBody", CubeListBuilder.create().texOffs(0, 102).addBox(-30.0F, -36.0F, -14.0F, 56.0F, 60.0F, 58.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = mainBody.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(252, 0).addBox(-69.0F, -40.5188F, 33.1184F, 44.0F, 56.0F, 26.0F, new CubeDeformation(0F)), PartPose.offset(45.0F, 6.5188F, 10.8816F));

        PartDefinition tail3 = tail.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(260, 172).addBox(-18.0F, 14.0F, -7.0F, 36.0F, 44.0F, 22.0F, new CubeDeformation(0F)), PartPose.offset(-47.0F, -50.5188F, 66.1184F));

        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(276, 360).addBox(0.0F, -26.0F, 6.0F, 0.0F, 12.0F, 16.0F, new CubeDeformation(0F))
                .texOffs(260, 274).addBox(-14.0F, -14.0F, 0.0F, 28.0F, 28.0F, 20.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, 28.0F, 15.0F));

        PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(324, 322).addBox(-12.0F, -11.0F, 0.0F, 24.0F, 22.0F, 18.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -3.0F, 20.0F));

        PartDefinition tail6 = tail5.addOrReplaceChild("tail6", CubeListBuilder.create().texOffs(356, 274).addBox(-10.0F, -9.0F, 0.0F, 20.0F, 18.0F, 18.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -2.0F, 18.0F));

        PartDefinition tail7 = tail6.addOrReplaceChild("tail7", CubeListBuilder.create().texOffs(204, 362).addBox(-8.0F, -7.0F, 0.0F, 16.0F, 14.0F, 16.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -2.0F, 18.0F));

        PartDefinition tailEnd = tail7.addOrReplaceChild("tailEnd", CubeListBuilder.create().texOffs(-32, 338).addBox(-28.3839F, -0.5F, 1.9601F, 56.0F, 0.0F, 32.0F, new CubeDeformation(0F))
                .texOffs(116, 338).addBox(-6.0F, -4.0F, 0.0F, 12.0F, 8.0F, 8.0F, new CubeDeformation(0F)), PartPose.offset(0.0F, -3.0F, 16.0F));

        PartDefinition rightFin = mainBody.addOrReplaceChild("rightFin", CubeListBuilder.create().texOffs(218, 324).addBox(-20.0F, 0.0F, -14.0F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0F)), PartPose.offset(-30.0F, -10.0F, 33.5F));

        PartDefinition rightFinOuter = rightFin.addOrReplaceChild("rightFinOuter", CubeListBuilder.create().texOffs(110, 304).addBox(-20.0F, 0.0F, -12.5F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0F)), PartPose.offset(-20.0F, 0.0F, -1.5F));

        PartDefinition leftFin = mainBody.addOrReplaceChild("leftFin", CubeListBuilder.create().texOffs(262, 240).addBox(0.0F, 0.0F, -14.0F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0F)), PartPose.offset(26.0F, -10.0F, 34.0F));

        PartDefinition leftFinOuter = leftFin.addOrReplaceChild("leftFinOuter", CubeListBuilder.create().texOffs(2, 304).addBox(0.0F, 0.0F, -13.0F, 20.0F, 0.0F, 32.0F, new CubeDeformation(0F)), PartPose.offset(20.0F, 0.0F, -1.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public ModelPart root() {
        return root;
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