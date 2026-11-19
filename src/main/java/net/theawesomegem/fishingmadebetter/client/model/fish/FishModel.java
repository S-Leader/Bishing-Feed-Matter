package net.theawesomegem.fishingmadebetter.client.model.fish;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.renderer.FishRenderState;

import java.util.ArrayList;
import java.util.List;

public class FishModel extends EntityModel<FishRenderState> {
    private final ModelPart root;
    private final List<SwimAnimation> animations = new ArrayList<>();

    public FishModel(EntityRendererProvider.Context context, ModelLayerLocation layer) {
        super(context.bakeLayer(layer));
        root = super.root();
    }

    protected static ModelLayerLocation createModelLocation(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, name), "main");
    }

    protected void addSwimAnimation(String path, float speed, float amplitude) {
        addSwimAnimation(path, speed, amplitude, 0);
    }

    protected void addSwimAnimation(String path, float speed, float amplitude, float phase) {
        addSwimAnimation(path, speed, amplitude, phase, 0, SwimAxis.Y);
    }

    protected void addSwimAnimation(String path, float speed, float amplitude, float phase, float offset, SwimAxis axis) {
        ModelPart part = root;
        for (String child : path.split("/")) part = part.getChild(child);
        animations.add(new SwimAnimation(part, speed, amplitude, phase, offset, axis));
    }

    public float swimAnimSpeed() {
        return 0.4F;
    }

    public float swimAnimScale() {
        return 1;
    }

    public float shadowRadius() {
        return 0.3F;
    }

    public boolean flipInAir() {
        return true;
    }

    public double xTiltScale() {
        return 1;
    }

    @Override
    public void setupAnim(FishRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        for (SwimAnimation animation : animations) {
            float angle = Mth.sin(age * animation.speed + animation.phase) * animation.amplitude * swimAnimScale() + animation.offset;
            switch (animation.axis) {
                case X -> animation.part.xRot += angle;
                case Y -> animation.part.yRot += angle;
                case Z -> animation.part.zRot += angle;
            }
        }
    }

    protected enum SwimAxis {
        X, Y, Z
    }

    private record SwimAnimation(ModelPart part, float speed, float amplitude, float phase, float offset,
                                 SwimAxis axis) {
    }
}
