package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ReaverSharkModel extends FishModel {
    public static final ModelLayerLocation MODEL_LOCATION = createModelLocation("reaver_shark");

    public ReaverSharkModel(EntityRendererProvider.Context context) {
        super(context, MODEL_LOCATION);
        float speed = swimAnimSpeed();
        this.addSwimAnimation("front/head", speed, -0.075f, -1f);
        this.addSwimAnimation("front", speed, -0.07f);
        this.addSwimAnimation("rear", speed, 0.13f);
        this.addSwimAnimation("rear/tail", speed, 0.15f, -1f);
        this.addSwimAnimation("rear/tail/fin", speed, 0.17f, -1.5f);
    }

    @Override
    public float shadowRadius() {
        return 0.8f;
    }

    @Override
    public float swimAnimSpeed() {
        return 0.2f;
    }

    @Override
    public float swimAnimScale() {
        return 0.85f;
    }

    @Override
    public double xTiltScale() {
        return 1.85f;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition front = partdefinition.addOrReplaceChild("front", CubeListBuilder.create().texOffs(3, 23).addBox(-2.0F, -3.5F, -5.0F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 20.325F, -7.0F));

        PartDefinition cube_r1 = front.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(35, 35).addBox(-7.7836F, 0.0F, -2.4763F, 8.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 1.5F, -0.5F, 0.0F, 0.2182F, -0.5236F));

        PartDefinition cube_r2 = front.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(2, 35).addBox(-0.2164F, 0.0F, -2.4763F, 7.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 1.5F, -0.5F, 0.0F, -0.2182F, 0.5236F));

        PartDefinition head = front.addOrReplaceChild("head", CubeListBuilder.create().texOffs(63, 10).addBox(-1.0F, 0.5F, -6.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(1, 43).addBox(-1.5F, -2.5F, -6.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(28, 106).addBox(0.0F, -9.9F, -5.975F, 0.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(28, 114).addBox(0.0F, 3.2F, -5.975F, 0.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -5.0F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(2, 123).addBox(0.9192F, 0.9192F, -6.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(3, 83).addBox(-3.1213F, -3.1213F, -6.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.025F, 0.0F, 0.0F, 0.7854F));

        PartDefinition rear = partdefinition.addOrReplaceChild("rear", CubeListBuilder.create().texOffs(30, 24).addBox(-2.0F, -3.2181F, -0.0048F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(48, 44).addBox(0.0F, -6.2181F, 0.9952F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 58).addBox(0.0F, 1.7819F, 0.9952F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 19.825F, -0.25F, -0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r4 = rear.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(42, 59).addBox(-5.4069F, -0.25F, -2.6481F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.7383F, 2.4943F, 0.0F, 0.3491F, -0.5236F));

        PartDefinition cube_r5 = rear.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(47, 54).addBox(0.4069F, -0.25F, -2.6481F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.7383F, 2.4943F, 0.0F, -0.3491F, 0.5236F));

        PartDefinition tail = rear.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(47, 1).addBox(-0.5F, 5.8488F, -0.0799F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -8.175F, 3.75F, -0.0873F, 0.0F, 0.0F));

        PartDefinition fin = tail.addOrReplaceChild("fin", CubeListBuilder.create().texOffs(30, 69).addBox(0.0F, 6.3488F, -0.0799F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 4.0F));

        PartDefinition bottom_r1 = fin.addOrReplaceChild("bottom_r1", CubeListBuilder.create().texOffs(12, 58).addBox(0.0F, -6.5383F, -1.113F, 0.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 8.4683F, 4.9116F, -2.5307F, 0.0F, 0.0F));

        PartDefinition top_r1 = fin.addOrReplaceChild("top_r1", CubeListBuilder.create().texOffs(54, 18).addBox(0.0F, -10.113F, -2.4617F, 0.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 8.4683F, 4.9116F, -0.9599F, 0.0F, 0.0F));

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(8, 6).addBox(-2.0F, -7.675F, -7.0F, 5.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(29, 46).addBox(0.5F, -11.675F, -6.0F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
