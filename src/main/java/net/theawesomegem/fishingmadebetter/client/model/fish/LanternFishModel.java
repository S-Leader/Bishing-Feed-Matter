package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class LanternFishModel extends FishModel {
    public static final ModelLayerLocation MODEL_LOCATION = createModelLocation("lantern_fish");

    public LanternFishModel(EntityRendererProvider.Context context) {
        super(context, MODEL_LOCATION);
        this.addSwimAnimation("front/lure", 0.45F, -0.1F);
        this.addSwimAnimation("rear", 0.45F, 0.2F);
        this.addSwimAnimation("rear/tail", 0.45F, 0.15F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition front = partdefinition.addOrReplaceChild("front", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 22.45F, 0.0F, -1.5708F, 0.0F, 0.0F));

        front.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -2.2172F, -2.0118F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 1.1F, -1.15F, 0.3491F, 0.0F, 3.1416F));

        PartDefinition lure = front.addOrReplaceChild("lure", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

        lure.addOrReplaceChild("mid_r1", CubeListBuilder.create().texOffs(14, 22).addBox(-0.1F, -0.4584F, -1.9533F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.8F, -3.625F, 1.0036F, 0.0F, 0.0F));

        lure.addOrReplaceChild("end_r1", CubeListBuilder.create().texOffs(10, 22).addBox(-0.1F, -1.0095F, -1.9479F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.7F, -4.225F, 2.3126F, 0.0F, 0.0F));

        lure.addOrReplaceChild("bulb_r1", CubeListBuilder.create().texOffs(27, 23).addBox(0.9F, 5.0546F, -1.1497F, -2.0F, -2.0F, -2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 2.8F, -3.625F, 0.7418F, 0.0F, 0.0F));

        lure.addOrReplaceChild("bulb_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-1.1F, 2.0591F, -1.4237F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 4.7F, -4.225F, 0.7418F, 0.0F, 0.0F));

        PartDefinition jaw = front.addOrReplaceChild("jaw", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

        jaw.addOrReplaceChild("d_tooth_l_r1", CubeListBuilder.create().texOffs(11, 18).addBox(-1.5F, -1.4907F, 1.0054F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.125F, -0.7F, 0.075F, 1.3788F, 0.0F, 3.1416F));

        jaw.addOrReplaceChild("d_lip_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -0.5253F, -0.0012F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -0.7F, 0.1F, 1.3788F, 0.0F, 3.1416F));

        jaw.addOrReplaceChild("d_tooth_f_r1", CubeListBuilder.create().texOffs(12, 10).addBox(-1.5F, -3.416F, 8.4435F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -5.425F, 1.125F, 1.3788F, 0.0F, 3.1416F));

        jaw.addOrReplaceChild("d_tooth_r_r1", CubeListBuilder.create().texOffs(15, 1).addBox(1.5F, -1.4907F, 1.1054F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.075F, -0.8F, 0.05F, 1.3788F, 0.0F, 3.1416F));

        PartDefinition palate = front.addOrReplaceChild("palate", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

        palate.addOrReplaceChild("u_tooth_l_r1", CubeListBuilder.create().texOffs(0, 23).addBox(-1.5F, -1.7205F, 0.9644F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 22).addBox(-1.5F, -1.7205F, 0.9644F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 1.625F, -2.35F, 0.3491F, 0.0F, 3.1416F));

        palate.addOrReplaceChild("u_tooth_r_r1", CubeListBuilder.create().texOffs(8, 18).addBox(1.5F, -2.5518F, 0.719F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 0.925F, -1.825F, 0.3491F, 0.0F, 3.1416F));

        palate.addOrReplaceChild("u_lip_r1", CubeListBuilder.create().texOffs(10, 12).addBox(-1.5F, -2.2172F, -0.0118F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 1.15F, -2.2F, 0.3491F, 0.0F, 3.1416F));

        PartDefinition rear = partdefinition.addOrReplaceChild("rear", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 22.45F, 2.0F, -1.5708F, 0.0F, 0.0F));

        rear.addOrReplaceChild("hind_r1", CubeListBuilder.create().texOffs(12, 5).addBox(-1.5F, -1.3493F, -1.8174F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(20, 21).addBox(-0.5F, -3.1493F, -3.9174F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -0.175F, -0.575F, 1.5708F, 0.0F, -3.1416F));

        PartDefinition tail = rear.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, 8).addBox(-0.1F, -6.05F, -0.7F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(18, 13).addBox(-0.1F, -8.05F, -1.7F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(18, 10).addBox(-1.1F, -5.025F, -1.4F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 22.45F, 0.0F, -1.5708F, 0.0F, 0.0F));

        body.addOrReplaceChild("fin_l_r1", CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, -2.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, -1.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

        body.addOrReplaceChild("fin_r_r1", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -2.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -1.7F, 0.0F, 0.0F, 0.0F, -0.3927F));

        body.addOrReplaceChild("main_r1", CubeListBuilder.create().texOffs(0, 5).addBox(-1.2F, -2.2245F, -1.1293F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.2F, -1.1F, -0.225F, 1.5708F, 0.0F, -3.1416F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}
