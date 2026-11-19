package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PufferfishBombSmallModel extends FishModel {
    public static final ModelLayerLocation MODEL_LOCATION = createModelLocation("pufferfish_bomb_small");

    @Override
    public boolean flipInAir() {
        return false;
    }

    @Override
    public double xTiltScale() {
        return 0.0f;
    }

    public PufferfishBombSmallModel(EntityRendererProvider.Context context) {
        super(context, MODEL_LOCATION);
        addSwimAnimation("right_fin", 0.2f, 0.4f, 0.0f, -0.2f, SwimAxis.Z);

        addSwimAnimation("left_fin", 0.2f, 0.4f, (float) Math.PI, 0.2f, SwimAxis.Z);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(6, 6).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 21.0F, -1.5F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 22.0F, -0.5F));

        partdefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(6, 6).addBox(0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 21.0F, -1.5F));

        partdefinition.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(26, 21).addBox(1.0F, -3.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 21).addBox(3.0F, -3.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 22.0F, -0.5F));

        partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(22, 16).addBox(-1.5F, -3.0F, 1.5F, 3.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 4).addBox(0.0F, -6.0F, 0.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

}
