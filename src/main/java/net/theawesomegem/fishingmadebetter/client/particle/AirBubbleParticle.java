package net.theawesomegem.fishingmadebetter.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class AirBubbleParticle extends SingleQuadParticle {
    private AirBubbleParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, sprites.get(RandomSource.create()));
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
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            AirBubbleParticle particle = new AirBubbleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
            return particle;
        }
    }
}
