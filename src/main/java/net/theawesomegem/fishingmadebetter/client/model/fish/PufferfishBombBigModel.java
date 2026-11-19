package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PufferfishBombBigModel extends FishModel {
    public static final ModelLayerLocation MODEL_LOCATION =
            createModelLocation("pufferfish_bomb_big");

    public PufferfishBombBigModel(EntityRendererProvider.Context context) {
        super(context, MODEL_LOCATION);
        addSwimAnimation("right_fin", 0.2f, 0.4f, 0.0f, -0.2f, SwimAxis.Z);
        addSwimAnimation("left_fin", 0.2f, 0.4f, (float) Math.PI, 0.2f, SwimAxis.Z);
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

        partdefinition.addOrReplaceChild(
                "right_fin",
                CubeListBuilder.create()
                        .texOffs(3, 6)
                        .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.0F, 17.0F, -3.0F));

        partdefinition.addOrReplaceChild(
                "left_fin",
                CubeListBuilder.create()
                        .texOffs(7, 6)
                        .addBox(0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.0F, 17.0F, -3.0F));

        PartDefinition bb_main =
                partdefinition.addOrReplaceChild(
                        "bb_main",
                        CubeListBuilder.create()
                                .texOffs(0, 21)
                                .addBox(
                                        0.0F,
                                        -7.0F,
                                        -1.0F,
                                        0.0F,
                                        2.0F,
                                        2.0F,
                                        new CubeDeformation(0.0F))
                                .texOffs(0, 16)
                                .addBox(
                                        -4.0F,
                                        -8.0F,
                                        -4.0F,
                                        8.0F,
                                        8.0F,
                                        8.0F,
                                        new CubeDeformation(0.0F))
                                .texOffs(0, 4)
                                .addBox(
                                        0.0F,
                                        -10.0F,
                                        -1.0F,
                                        0.0F,
                                        2.0F,
                                        2.0F,
                                        new CubeDeformation(0.0F)),
                        PartPose.offset(0.0F, 24.0F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 4.0F, 0.0F, 0.7854F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r2",
                CubeListBuilder.create()
                        .texOffs(2, 8)
                        .addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, 0.0F, 4.0F, 0.0F, -0.7854F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r3",
                CubeListBuilder.create()
                        .texOffs(15, 3)
                        .addBox(-6.0F, 0.0F, 0.0F, 8.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, 0.0F, 4.0F, -0.7854F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r4",
                CubeListBuilder.create()
                        .texOffs(16, 2)
                        .addBox(-7.0F, -1.0F, 0.0F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -8.0F, 4.0F, -0.7854F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r5",
                CubeListBuilder.create()
                        .texOffs(15, 0)
                        .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.7854F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r6",
                CubeListBuilder.create()
                        .texOffs(6, 9)
                        .addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, 0.0F, -4.0F, 0.0F, -0.7854F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r7",
                CubeListBuilder.create()
                        .texOffs(4, 9)
                        .addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, 0.0F, -4.0F, 0.0F, 0.7854F, 0.0F));

        bb_main.addOrReplaceChild(
                "cube_r8",
                CubeListBuilder.create()
                        .texOffs(16, 1)
                        .addBox(-2.0F, -1.0F, 0.0F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -8.0F, -3.9F, 0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}
