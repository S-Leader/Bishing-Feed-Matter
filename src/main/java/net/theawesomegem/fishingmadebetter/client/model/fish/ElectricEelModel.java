package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ElectricEelModel extends FishModel {
    public static final ModelLayerLocation MODEL_LOCATION = createModelLocation("electric_eel");

    public ElectricEelModel(EntityRendererProvider.Context context) {
        super(context, MODEL_LOCATION);
        float speed = swimAnimSpeed();
        this.addSwimAnimation("front/head", speed, -0.15f, -1f);
        this.addSwimAnimation("rear", speed, 0.25f, -1f);
        this.addSwimAnimation("rear/tail", speed, 0.25f, -1.5f);
    }

    @Override
    public float swimAnimSpeed() {
        return 0.3f;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition front = partdefinition.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -3.0F, 0.0F, 3.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 33).addBox(0.0F, -4.0F, 1.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, -9.0F));

        front.addOrReplaceChild("fin_r_r1", CubeListBuilder.create().texOffs(0, 30).addBox(-0.1F, 0.0F, -0.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -1.0F, 0.5F, 0.0F, -0.5236F, 0.0F));

        front.addOrReplaceChild("fin_l_r1", CubeListBuilder.create().texOffs(26, 27).addBox(0.1F, 0.0F, -0.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -1.0F, 0.5F, 0.0F, 0.5236F, 0.0F));

        front.addOrReplaceChild("head", CubeListBuilder.create().texOffs(39, 3).addBox(-1.5F, -2.5F, -4.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(26, 23).addBox(-1.5F, 0.5F, -3.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 0.0F));

        PartDefinition rear = partdefinition.addOrReplaceChild("rear", CubeListBuilder.create().texOffs(16, 14).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(15, 29).addBox(0.0F, -4.0F, 0.0F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 1.0F));

        rear.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(26, 7).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(33, 29).addBox(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 6.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}