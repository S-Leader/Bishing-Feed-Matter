package net.theawesomegem.fishingmadebetter.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public final class AirBubbleParticle extends TextureSheetParticle {
    private AirBubbleParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);
        setSize(0.02F, 0.02F);
        quadSize *= random.nextFloat() * 0.6F + 0.2F;
        xd = xSpeed * 0.2 + (random.nextDouble() * 2 - 1) * 0.02;
        yd = ySpeed * 0.2 + (random.nextDouble() * 2 - 1) * 0.02;
        zd = zSpeed * 0.2 + (random.nextDouble() * 2 - 1) * 0.02;
        lifetime = (int) (8 / (random.nextDouble() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (lifetime-- <= 0) {
            remove();
            return;
        }
        yd += 0.002;
        move(xd, yd, zd);
        xd *= 0.85;
        yd *= 0.85;
        zd *= 0.85;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            AirBubbleParticle particle = new AirBubbleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
