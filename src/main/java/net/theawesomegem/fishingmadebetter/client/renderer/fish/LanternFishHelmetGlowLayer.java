package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.LanternFishHelmetModel;
import net.theawesomegem.fishingmadebetter.registry.ModItems;

public final class LanternFishHelmetGlowLayer<
        S extends HumanoidRenderState, M extends EntityModel<S>>
        extends RenderLayer<S, M> {
    private static final Identifier GLOW =
            Identifier.fromNamespaceAndPath(
                    Constants.MOD_ID, "textures/entity/fish/lantern_fish_helmet_glowmask.png");
    private final LanternFishHelmetModel model = new LanternFishHelmetModel();

    public LanternFishHelmetGlowLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void submit(
            PoseStack pose,
            SubmitNodeCollector collector,
            int light,
            S state,
            float yaw,
            float pitch) {
        if (!state.isInvisible && state.headEquipment.is(ModItems.LANTERN_FISH_ITEM.get()))
            collector
                    .order(2)
                    .submitModel(
                            model,
                            state,
                            pose,
                            RenderTypes.eyes(GLOW),
                            15728880,
                            OverlayTexture.NO_OVERLAY,
                            state.outlineColor,
                            null);
    }
}
