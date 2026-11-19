package net.theawesomegem.fishingmadebetter.client.renderer.fish;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PinkStarfishProjectile;
import org.joml.Quaternionf;

public final class StarfishProjectileRenderer
        extends EntityRenderer<PinkStarfishProjectile, StarfishProjectileRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public StarfishProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.getItemModelResolver();
    }

    public static final class State extends EntityRenderState {
        public final Quaternionf rotation = new Quaternionf();
        public final ItemStackRenderState item = new ItemStackRenderState();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PinkStarfishProjectile fish, State state, float partialTick) {
        super.extractRenderState(fish, state, partialTick);
        state.rotation.set(fish.getRenderRotation(partialTick));
        itemModelResolver.updateForNonLiving(
                state.item, fish.getItem(), ItemDisplayContext.FIXED, fish);
    }

    @Override
    public void submit(
            State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.mulPose(state.rotation);
        pose.scale(.5F, .5F, .5F);
        state.item.submit(
                pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}
