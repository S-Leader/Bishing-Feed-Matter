package net.theawesomegem.fishingmadebetter.client.particle;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class ElectricParticleRenderTypes {
    public static final ParticleRenderType ZAP = new ParticleRenderType("fishingmadebetter:zap");
    public static final RenderPipeline SPARK_PIPELINE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET).withLocation(Identifier.fromNamespaceAndPath("fishingmadebetter", "pipeline/spark")).withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING)).withCull(false).build();
    public static final SingleQuadParticle.Layer SPARK = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, SPARK_PIPELINE);

    public static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(SPARK_PIPELINE);
    }
}
