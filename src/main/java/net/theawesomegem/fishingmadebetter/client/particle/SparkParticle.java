package net.theawesomegem.fishingmadebetter.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SparkParticle extends TextureSheetParticle {
    private Vec3 direction;
    private final SpriteSet sprites;
    private float roll;
    private float lastRoll;

    public SparkParticle(ClientLevel level, double x, double y, double z,
                         double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
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
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 cameraPos = renderInfo.getPosition();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        Vec3 dir = this.direction;
        float interpRoll = Mth.lerp(partialTicks, this.lastRoll, this.roll);

        Vector3f dirVec = new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);
        Quaternionf q = new Quaternionf().rotateTo(new Vector3f(0, 1, 0), dirVec);
        q.rotateAxis(interpRoll, dirVec.x, dirVec.y, dirVec.z);

        float size = this.getQuadSize(0);
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-size, -size, 0),
                new Vector3f(-size, size, 0),
                new Vector3f(size, size, 0),
                new Vector3f(size, -size, 0)
        };

        for (Vector3f v : vertices) {
            v.rotate(q);
            v.add(x, y, z);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);
        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        float a = this.alpha;

        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(u0, v0).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(u0, v1).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(u1, v1).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(u1, v0).color(r, g, b, a).uv2(light).endVertex();

        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(u0, v0).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(u1, v0).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(u1, v1).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(u0, v1).color(r, g, b, a).uv2(light).endVertex();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new SparkParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return GLOWING_PARTICLE;
    }

    public static final ParticleRenderType GLOWING_PARTICLE = ElectricParticleRenderTypes.SPARK;
}