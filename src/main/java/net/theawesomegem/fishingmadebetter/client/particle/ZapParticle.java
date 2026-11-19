package net.theawesomegem.fishingmadebetter.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.theawesomegem.fishingmadebetter.common.particle.ZapParticleOption;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ZapParticle extends SingleQuadParticle {
    private final Vec3 destination;
    private final List<Vec3> segments = new ArrayList<>();
    private int nextSegmentUpdate = 0;

    public ZapParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, ZapParticleOption options, SpriteSet sprites) {
        super(pLevel, pX, pY, pZ, 0, 0, 0, sprites.get(RandomSource.create()));
        this.setSize(1, 1);
        this.quadSize = 1f;
        this.destination = options.destination();
        this.lifetime = random.nextIntBetweenInclusive(3, 8);
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        if (this.age >= nextSegmentUpdate) {
            regenerateSegments();
            nextSegmentUpdate = this.age + 3;
        }
    }

    private void regenerateSegments() {
        segments.clear();
        Vec3 startPos = new Vec3(0, 0, 0);
        Vec3 endPos = destination.subtract(x, y, z);

        int numSegments = random.nextIntBetweenInclusive(3, 6);
        double totalLength = startPos.distanceTo(endPos);
        double segLength = totalLength / numSegments;
        Vec3 step = endPos.subtract(startPos).scale(1.0 / numSegments);

        segments.add(startPos);
        for (int i = 1; i < numSegments; i++) {
            Vec3 base = startPos.add(step.scale(i));

            Vec3 offset = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize().scale(segLength * 0.5);
            base = base.add(offset);
            segments.add(base);
        }
        segments.add(endPos);
    }

    private void setRGBA(float r, float g, float b, float a) {
        this.rCol = r * a;
        this.gCol = g * a;
        this.bCol = b * a;
        this.alpha = 1;
    }

    private void drawSegment(List<ZapParticleGroup.Vertex> consumer, float f, float f1, float f2, Vector3f start, Vector3f end, RandomSource random) {
        Vector3f d = new Vector3f(end.x() - start.x(), end.y() - start.y(), end.z() - start.z());
        d.normalize();
        Vec2 heading = new Vec2((float) Math.asin(-d.y()), (float) -Mth.atan2(d.x(), d.z()));

        setRGBA(1, 1, 1, 1);
        tube(consumer, f, f1, f2, heading, start, end, 0.03f);

        setRGBA(0.25f, 0.7f, 1, 0.3f);
        tube(consumer, f, f1, f2, heading, start, end, 0.05f);

        setRGBA(0.25f, 0.7f, 1, 0.15f);
        tube(consumer, f, f1, f2, heading, start, end, 0.12f);
    }

    private void tube(List<ZapParticleGroup.Vertex> consumer, float f, float f1, float f2, Vec2 heading, Vector3f start, Vector3f end, float width) {
        float h = width * .5f;

        Vector3f[] left = new Vector3f[]{new Vector3f(-h * Mth.cos(heading.y) + start.x(), -h + start.y(), start.z() - h * Mth.sin(heading.y)), new Vector3f(-h * Mth.cos(heading.y) + start.x(), h + start.y(), start.z() - h * Mth.sin(heading.y)), new Vector3f(-h * Mth.cos(heading.y) + end.x(), h + end.y(), end.z() - h * Mth.sin(heading.y)), new Vector3f(-h * Mth.cos(heading.y) + end.x(), -h + end.y(), end.z() - h * Mth.sin(heading.y))};
        Vector3f[] right = new Vector3f[]{new Vector3f(h * Mth.cos(heading.y) + end.x(), -h + end.y(), end.z() + h * Mth.sin(heading.y)), new Vector3f(h * Mth.cos(heading.y) + end.x(), h + end.y(), end.z() + h * Mth.sin(heading.y)), new Vector3f(h * Mth.cos(heading.y) + start.x(), h + start.y(), start.z() + h * Mth.sin(heading.y)), new Vector3f(h * Mth.cos(heading.y) + start.x(), -h + start.y(), start.z() + h * Mth.sin(heading.y))};
        Vector3f[] top = new Vector3f[]{new Vector3f(h * Mth.cos(heading.y) + start.x(), -h + start.y(), start.z() + h * Mth.sin(heading.y)), new Vector3f(-h * Mth.cos(heading.y) + start.x(), -h + start.y(), start.z() - h * Mth.sin(heading.y)), new Vector3f(-h * Mth.cos(heading.y) + end.x(), -h + end.y(), end.z() - h * Mth.sin(heading.y)), new Vector3f(h * Mth.cos(heading.y) + end.x(), -h + end.y(), end.z() + h * Mth.sin(heading.y))};
        Vector3f[] bottom = new Vector3f[]{new Vector3f(h * Mth.cos(heading.y) + end.x(), h + end.y(), end.z() + h * Mth.sin(heading.y)), new Vector3f(-h * Mth.cos(heading.y) + end.x(), h + end.y(), end.z() - h * Mth.sin(heading.y)), new Vector3f(-h * Mth.cos(heading.y) + start.x(), h + start.y(), start.z() - h * Mth.sin(heading.y)), new Vector3f(h * Mth.cos(heading.y) + start.x(), h + start.y(), start.z() + h * Mth.sin(heading.y))};

        quad(consumer, f, f1, f2, left);
        quad(consumer, f, f1, f2, right);
        quad(consumer, f, f1, f2, top);
        quad(consumer, f, f1, f2, bottom);
    }

    private void makeCornerVertex(List<ZapParticleGroup.Vertex> pConsumer, Vector3f pVec3f, float p_233996_, float p_233997_, int p_233998_) {
        pConsumer.add(new ZapParticleGroup.Vertex(pVec3f.x(), pVec3f.y(), pVec3f.z(), p_233996_, p_233997_, rCol, gCol, bCol, alpha));
    }

    private void quad(List<ZapParticleGroup.Vertex> pConsumer, float f, float f1, float f2, Vector3f[] avector3f) {
        float f3 = this.getQuadSize(0);
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }
        int j = this.getLightCoords(0);
        this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);
    }

    public void extractGeometry(List<ZapParticleGroup.Vertex> consumer, Camera camera, float partialTick) {
        Vec3 camPos = camera.position();
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - camPos.x());
        float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - camPos.y());
        float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - camPos.z());

        if (segments.isEmpty()) {
            regenerateSegments();
        }

        RandomSource random = RandomSource.create();
        for (int i = 0; i < segments.size() - 1; i++) {
            Vec3 startWorld = segments.get(i);
            Vec3 endWorld = segments.get(i + 1);
            Vector3f start = new Vector3f((float) startWorld.x, (float) startWorld.y, (float) startWorld.z);
            Vector3f end = new Vector3f((float) endWorld.x, (float) endWorld.y, (float) endWorld.z);
            drawSegment(consumer, f, f1, f2, start, end, random);
        }
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public ParticleRenderType getGroup() {
        return PARTICLE_EMISSIVE;
    }

    public boolean shouldCull() {
        return false;
    }

    public static final ParticleRenderType PARTICLE_EMISSIVE = ElectricParticleRenderTypes.ZAP;

    @Override
    public int getLightCoords(float pPartialTick) {
        return 15728880;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ZapParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull ZapParticleOption options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, RandomSource random) {
            var particle = new ZapParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options, sprite);
            particle.setAlpha(1.0F);
            return particle;
        }
    }
}
