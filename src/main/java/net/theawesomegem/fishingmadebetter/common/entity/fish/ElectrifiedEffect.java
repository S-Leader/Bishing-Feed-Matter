package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.theawesomegem.fishingmadebetter.common.particle.ZapParticleOption;
import net.theawesomegem.fishingmadebetter.registry.ModEffects;

import java.util.ArrayList;
import java.util.List;

public class ElectrifiedEffect extends MobEffect {
    public ElectrifiedEffect() {
        super(MobEffectCategory.HARMFUL, 0x59C7FF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof ElectricEel) {
            return true;
        }
        if (level.getGameTime() % 8 == 0) {
            for (int i = 0; i < 2; i++) {
                Vec3 start =
                        new Vec3(
                                entity.getX()
                                        + (entity.getRandom().nextDouble() - 0.5)
                                        * entity.getBbWidth(),
                                entity.getY(),
                                entity.getZ()
                                        + (entity.getRandom().nextDouble() - 0.5)
                                        * entity.getBbWidth());
                arc(level, start, entity.getBoundingBox().getCenter());
            }
        }
        if (level.getGameTime() % 20 != 0) {
            return true;
        }
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(entity);
        for (int i = 0; i < targets.size() && targets.size() < 5; i++) {
            LivingEntity current = targets.get(i);
            for (LivingEntity candidate :
                    level.getEntitiesOfClass(
                            LivingEntity.class,
                            current.getBoundingBox().inflate(5),
                            other ->
                                    other.isAlive()
                                            && other.hasEffect(ModEffects.ELECTRIFIED)
                                            && !targets.contains(other)
                                            && current.hasLineOfSight(other))) {
                if (targets.size() >= 5) break;
                if (current.distanceToSqr(candidate) <= 25) {
                    targets.add(candidate);
                    arc(
                            level,
                            current.getBoundingBox().getCenter(),
                            candidate.getBoundingBox().getCenter());
                }
            }
        }
        for (LivingEntity target : targets) {
            target.hurtOrSimulate(target.damageSources().lightningBolt(), targets.size());
        }
        return true;
    }

    public static void arc(ServerLevel level, Vec3 start, Vec3 end) {
        level.sendParticles(new ZapParticleOption(end), start.x, start.y, start.z, 1, 0, 0, 0, 0);
    }
}
