package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PinkStarfishModel extends FishModel {
    public static final ModelLayerLocation MODEL_LOCATION = createModelLocation("pink_starfish");


    public PinkStarfishModel(EntityRendererProvider.Context context) {
        super(context, MODEL_LOCATION);
    }

    @Override
    public float swimAnimScale() {
        return 0.0f;
    }

    @Override
    public boolean flipInAir() {
        return false;
    }

    @Override
    public double xTiltScale() {
        return 0.0f;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition legs = partdefinition.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(0, 7).addBox(3.0F, -2.1F, -2.4F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 24.0F, -3.0F));

        legs.addOrReplaceChild("leg_se_r1", CubeListBuilder.create().texOffs(12, 13).addBox(-2.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -0.1F, 3.5F, 0.0F, 2.5133F, 0.0F));

        legs.addOrReplaceChild("leg_sw_r1", CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -0.1F, 4.7F, 0.0F, -2.5133F, 0.0F));

        legs.addOrReplaceChild("leg_ne_r1", CubeListBuilder.create().texOffs(16, 0).addBox(-2.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -0.1F, 1.6F, 0.0F, 1.2566F, 0.0F));

        legs.addOrReplaceChild("leg_nw_r1", CubeListBuilder.create().texOffs(12, 7).addBox(-2.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7F, -0.1F, 3.5F, 0.0F, -1.2566F, 0.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(2.0F, -3.0F, 1.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 24.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}
