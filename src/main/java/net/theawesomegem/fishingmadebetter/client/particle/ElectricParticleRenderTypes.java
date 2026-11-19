package net.theawesomegem.fishingmadebetter.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public final class ElectricParticleRenderTypes {
    public static final ParticleRenderType ZAP = glowing(GlStateManager.SourceFactor.ONE);
    public static final ParticleRenderType SPARK = glowing(GlStateManager.SourceFactor.SRC_ALPHA);

    private ElectricParticleRenderTypes() {
    }

    private static ParticleRenderType glowing(GlStateManager.SourceFactor source) {
        return new ParticleRenderType() {
            @Override
            public void begin(BufferBuilder builder, TextureManager textures) {
                RenderSystem.depthMask(true);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                RenderSystem.setShader(GameRenderer::getParticleShader);
                RenderSystem.enableBlend();
                RenderSystem.disableCull();
                RenderSystem.blendFunc(source, GlStateManager.DestFactor.ONE);
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            @Override
            public void end(Tesselator tesselator) {
                tesselator.end();
                RenderSystem.enableCull();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        };
    }
}
