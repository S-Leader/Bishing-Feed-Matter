package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class LanternFishHelmetModel extends HumanoidModel<LivingEntity> {
    public LanternFishHelmetModel() {
        super(createLayer().bakeRoot());
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (String part : new String[]{"head", "hat", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            root.addOrReplaceChild(part, CubeListBuilder.create(), PartPose.ZERO);
        }
        PartDefinition head = root.getChild("head");
        PartDefinition bone = head.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition front = bone.addOrReplaceChild("front", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, -10.0000000F, 0.0000000F, -0.5235988F, 0.0000000F, 0.0000000F));
        front.addOrReplaceChild("cube0", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5000000F, -2.2171800F, -2.0118300F, 3.0000000F, 4.0000000F, 2.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, 1.3500000F, -1.5830100F, 0.3490659F, 0.0000000F, 3.1415927F));
        PartDefinition lure = front.addOrReplaceChild("lure", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 0.2500000F, 0.5669900F, 0.0000000F, 0.0000000F, 0.0000000F));
        lure.addOrReplaceChild("cube0", CubeListBuilder.create().texOffs(0, 18).addBox(-1.1000000F, 2.2065800F, -1.5588100F, 2.0000000F, 2.0000000F, 2.0000000F, new CubeDeformation(-0.2000000F)), PartPose.offsetAndRotation(0.0000000F, 4.3750000F, -4.6000000F, -0.0610865F, 0.0000000F, 0.0000000F));
        lure.addOrReplaceChild("cube1", CubeListBuilder.create().texOffs(27, 23).addBox(0.9000000F, 4.2065700F, 0.4411900F, -2.0000000F, -2.0000000F, -2.0000000F, new CubeDeformation(-0.1000000F)), PartPose.offsetAndRotation(0.0000000F, 4.3750000F, -4.6000000F, -0.0610865F, 0.0000000F, 0.0000000F));
        lure.addOrReplaceChild("cube2", CubeListBuilder.create().texOffs(10, 22).addBox(-0.1000000F, -1.1446000F, -2.0953100F, 0.0000000F, 1.0000000F, 2.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(0.0000000F, 4.3750000F, -4.6000000F, 1.5097098F, 0.0000000F, 0.0000000F));
        lure.addOrReplaceChild("cube3", CubeListBuilder.create().texOffs(14, 22).addBox(-0.1000000F, -0.4584200F, -1.9532900F, 0.0000000F, 1.0000000F, 2.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(0.0000000F, 2.8000000F, -3.6250000F, 0.7068583F, 0.0000000F, 0.0000000F));
        PartDefinition jaw = front.addOrReplaceChild("jaw", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, -0.6000000F, 0.5000000F, 1.3089969F, 0.0000000F, 0.0000000F));
        jaw.addOrReplaceChild("cube0", CubeListBuilder.create().texOffs(15, 1).addBox(1.5000000F, -1.4906900F, 1.1053500F, 0.0000000F, 1.0000000F, 3.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.0750000F, -0.8000000F, 0.0500000F, 1.3788101F, 0.0000000F, 3.1415927F));
        jaw.addOrReplaceChild("cube1", CubeListBuilder.create().texOffs(12, 10).addBox(-1.5000000F, -3.4159700F, 8.4435300F, 3.0000000F, 1.0000000F, 0.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, -5.4250000F, 1.1250000F, 1.3788101F, 0.0000000F, 3.1415927F));
        jaw.addOrReplaceChild("cube2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5000000F, -0.5252800F, -0.0012400F, 3.0000000F, 1.0000000F, 4.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, -0.7000000F, 0.1000000F, 1.3788101F, 0.0000000F, 3.1415927F));
        jaw.addOrReplaceChild("cube3", CubeListBuilder.create().texOffs(11, 18).addBox(-1.5000000F, -1.4906900F, 1.0053500F, 0.0000000F, 1.0000000F, 3.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1250000F, -0.7000000F, 0.0750000F, 1.3788101F, 0.0000000F, 3.1415927F));
        PartDefinition palate = front.addOrReplaceChild("palate", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 0.2500000F, 0.5669900F, 0.0000000F, 0.0000000F, 0.0000000F));
        palate.addOrReplaceChild("cube0", CubeListBuilder.create().texOffs(10, 12).addBox(-1.5000000F, -2.2171800F, -0.0118300F, 3.0000000F, 3.0000000F, 1.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, 1.1500000F, -2.2000000F, 0.3490659F, 0.0000000F, 3.1415927F));
        palate.addOrReplaceChild("cube1", CubeListBuilder.create().texOffs(8, 18).addBox(1.5000000F, -2.5517700F, 0.7189800F, 0.0000000F, 3.0000000F, 1.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, 0.9250000F, -1.8250000F, 0.3490659F, 0.0000000F, 3.1415927F));
        palate.addOrReplaceChild("cube2", CubeListBuilder.create().texOffs(0, 22).addBox(-1.5000000F, -1.7204900F, 0.9644400F, 3.0000000F, 0.0000000F, 1.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, 1.6000000F, -2.3500000F, 0.3490659F, 0.0000000F, 3.1415927F));
        palate.addOrReplaceChild("cube3", CubeListBuilder.create().texOffs(0, 23).addBox(-1.5000000F, -1.7204900F, 0.9644400F, 0.0000000F, 4.0000000F, 1.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, 1.6000000F, -2.3500000F, 0.3490659F, 0.0000000F, 3.1415927F));
        PartDefinition rear = bone.addOrReplaceChild("rear", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, -10.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail = rear.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        tail.addOrReplaceChild("cube0", CubeListBuilder.create().texOffs(18, 10).addBox(-1.0000000F, -1.5000000F, -1.0000000F, 2.0000000F, 1.0000000F, 2.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(0.0000000F, -3.3250000F, -0.4000000F, 0.0000000F, 0.0000000F, 0.4363323F));
        tail.addOrReplaceChild("cube1", CubeListBuilder.create().texOffs(22, 8).addBox(0.0000000F, -1.1000000F, -0.5000000F, 0.0000000F, 1.0000000F, 1.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(0.4250000F, -4.6750000F, -0.2000000F, 0.0000000F, 0.0000000F, 0.4363323F));
        tail.addOrReplaceChild("cube2", CubeListBuilder.create().texOffs(18, 13).addBox(-0.0555400F, -1.9823800F, -1.5000000F, 0.0000000F, 2.0000000F, 3.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(0.8750000F, -5.6000000F, -0.2000000F, 0.0000000F, 0.0000000F, 2.3561945F));
        PartDefinition hind = tail.addOrReplaceChild("hind", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        hind.addOrReplaceChild("cube0", CubeListBuilder.create().texOffs(20, 21).addBox(0.0000000F, -3.3493100F, -2.8173900F, 0.0000000F, 2.0000000F, 3.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(0.0250000F, -3.2750000F, -0.3750000F, 1.5707963F, 0.0000000F, -2.8797933F));
        hind.addOrReplaceChild("cube1", CubeListBuilder.create().texOffs(12, 5).addBox(-1.0000000F, -0.8493100F, -1.3173900F, 2.0000000F, 3.0000000F, 2.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.1000000F, -2.6750000F, -1.0750000F, 1.5707963F, 0.0000000F, -2.8797933F));
        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, -10.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        body.addOrReplaceChild("cube0", CubeListBuilder.create().texOffs(0, 5).addBox(-1.2000000F, -2.2245000F, -1.1292800F, 3.0000000F, 4.0000000F, 3.0000000F, new CubeDeformation(0.0100000F)), PartPose.offsetAndRotation(0.2000000F, -1.1000000F, -0.2250000F, 1.5707963F, 0.0000000F, -3.1415927F));
        body.addOrReplaceChild("cube1", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0000000F, -2.0000000F, -1.0000000F, 0.0000000F, 2.0000000F, 2.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(-0.6000000F, -1.7000000F, 0.0000000F, 0.0000000F, 0.0000000F, -0.3926991F));
        body.addOrReplaceChild("cube2", CubeListBuilder.create().texOffs(22, 4).addBox(-1.0000000F, -2.0000000F, -1.0000000F, 0.0000000F, 2.0000000F, 2.0000000F, new CubeDeformation(0.0000000F)), PartPose.offsetAndRotation(2.3000000F, -1.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.3926991F));
        return LayerDefinition.create(mesh, 32, 32);
    }
}
