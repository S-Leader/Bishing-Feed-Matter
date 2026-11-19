package net.theawesomegem.fishingmadebetter.client.model.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.renderer.WhipRenderState;

public class ElectricEelItemModel extends EntityModel<WhipRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "tentacle_spike"), "main");
    private static final int NUM_SEGMENTS = 15;
    private final ModelPart root;
    private final ModelPart[] segments;

    public ElectricEelItemModel(ModelPart root) {
        super(root.getChild("root"));
        this.root = root.getChild("root");
        ModelPart[] parts = new ModelPart[NUM_SEGMENTS];
        ModelPart last = this.root;
        for (int i = 0; i < NUM_SEGMENTS; i++) {
            last = last.getChild("segment" + (i + 1));
            parts[i] = last;
        }
        this.segments = parts;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition last = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        for (int i = 0; i < NUM_SEGMENTS; i++) {
            String segmentName = "segment" + (i + 1);
            CubeListBuilder segmentBuilder = CubeListBuilder.create();

            if (i == 0) {

                segmentBuilder.texOffs(22, 19).addBox(-1.0F, -13.0F, -2.0F, 2.0F, 6.0F, 3.0F).texOffs(14, 1).addBox(0.0F, -12.0F, -4.0F, 0.0F, 5.0F, 6.0F).texOffs(26, 0).addBox(-0.5F, -7.0F, -1.0F, 1.0F, 4.0F, 2.0F).texOffs(12, 14).addBox(0.0F, -7.0F, -3.0F, 0.0F, 7.0F, 5.0F);
            } else if (i == NUM_SEGMENTS - 1) {

                segmentBuilder.texOffs(26, 6).addBox(-1.5F, -3.5F, 0.0F, 3.0F, 3.0F, 1.0F).texOffs(22, 12).addBox(-1.5F, -4.5F, -3.0F, 3.0F, 4.0F, 3.0F);
            } else {

                segmentBuilder.texOffs(0, 0).addBox(-1.5F, -11.0F, -3.0F, 3.0F, 10.0F, 4.0F).texOffs(0, 14).addBox(0.0F, -11.0F, -4.0F, 0.0F, 11.0F, 6.0F);
            }

            float yOffset = (i == 0) ? 0.0F : -10.5F;
            last = last.addOrReplaceChild(segmentName, segmentBuilder, PartPose.offset(0.0F, yOffset, 0.0F));
        }

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(WhipRenderState state) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float progress = Mth.clamp(state.progress, 0, 1);
        for (int i = 0; i < NUM_SEGMENTS; i++) {
            ModelPart segment = segments[i];
            segment.xRot = Mth.lerp(progress, 120f / (i + 1), -120f / (i + 1)) * Mth.DEG_TO_RAD;
            if (i == 0) {
                float scale = (float) ((1 - Math.cos((1 - Math.abs(progress - 0.5f) * 2) * Math.PI)) / 2);
                segment.xScale = segment.yScale = segment.zScale = scale;
            }
        }
    }

    public void translateToSegment(PoseStack stack, int segmentIndex) {
        root.translateAndRotate(stack);
        for (int i = 0; i <= segmentIndex; i++) {
            segments[i].translateAndRotate(stack);
        }
    }
}
