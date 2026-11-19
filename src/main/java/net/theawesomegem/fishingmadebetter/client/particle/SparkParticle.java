package net.theawesomegem.fishingmadebetter.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SparkParticle extends SingleQuadParticle {
    private Vec3 direction;
    private final SpriteSet sprites;
    private float roll;
    private float lastRoll;

    public SparkParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz, spriteSet.get(RandomSource.create()));
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (speed > 1.0E-7) {
            this.direction = new Vec3(vx, vy, vz).normalize();
        } else {
            this.direction = new Vec3(0, 1, 0);
        }

        this.lifetime = 10 + this.random.nextInt(10);
        this.gravity = 0.1F;
        this.scale(this.random.nextFloat() + 0.5f);
        this.sprites = spriteSet;
        this.setSpriteFromAge(this.sprites);

        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.lastRoll = this.roll;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        double speed = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        if (speed > 1.0E-7) {
            this.direction = new Vec3(this.xd, this.yd, this.zd).normalize();
        }

        this.lastRoll = this.roll;
        this.roll += (this.random.nextFloat() - 0.5f) * 0.1f;

        float halfLife = this.lifetime / 2.0f;
        if (this.age > halfLife) {
            this.alpha = 1.0F - (this.age - halfLife) / halfLife;
        }
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        Vector3f dir = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);
        Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0, 1, 0), dir);
        rotation.rotateAxis(Mth.lerp(partialTick, lastRoll, roll), dir);
        extractRotatedQuad(state, camera, rotation, partialTick);
        rotation.rotateY(Mth.PI);
        extractRotatedQuad(state, camera, rotation, partialTick);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return ElectricParticleRenderTypes.SPARK;
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz, RandomSource random) {
            return new SparkParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
