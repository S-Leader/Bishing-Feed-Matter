package net.theawesomegem.fishingmadebetter.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public final class ZapParticleGroup extends ParticleGroup<ZapParticle> {
    public record Vertex(float x, float y, float z, float u, float v, float red, float green, float blue, float alpha) {
    }

    public ZapParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        List<Vertex> vertices = new ArrayList<>();
        for (ZapParticle particle : particles)
            particle.extractGeometry(vertices, camera, partialTick);
        var snapshot = List.copyOf(vertices);
        return (collector, cameraState) -> collector.submitCustomGeometry(new PoseStack(), RenderTypes.eyes(TextureAtlas.LOCATION_PARTICLES), (pose, buffer) -> {
            for (Vertex vertex : snapshot)
                buffer.addVertex(pose, vertex.x, vertex.y, vertex.z).setColor(vertex.red, vertex.green, vertex.blue, vertex.alpha).setUv(vertex.u, vertex.v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0, 1, 0);
        });
    }
}
