package net.theawesomegem.fishingmadebetter.common.entity.fish;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.registry.ModEffects;
import net.theawesomegem.fishingmadebetter.registry.ModSounds;

public class ElectricEel extends PortedFish {
    private int electricCooldown = 20;

    public ElectricEel(EntityType<? extends ElectricEel> type, Level level) {
        super(type, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isAlive() && level() instanceof ServerLevel && --electricCooldown <= 0) {
            electricCooldown = 20;
            double range = isInWaterRainOrBubble() ? 1 : 0.3;
            for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range), this::canShock)) {
                shock(target);
            }
        }
    }

    private boolean canShock(LivingEntity target) {
        return target.isAlive() && !(target instanceof ElectricEel)
                && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private void shock(LivingEntity target) {
        if (target.hurt(damageSources().mobAttack(this), 1)) {
            target.addEffect(new MobEffectInstance(ModEffects.ELECTRIFIED.get(), 60, 0, false, false), this);
            ElectrifiedEffect.arc((ServerLevel) level(), getBoundingBox().getCenter(), target.getBoundingBox().getCenter());
            playSound(ModSounds.EEL_RELEASE.get(), 1, 1);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != ModEffects.ELECTRIFIED.get() && super.canBeAffected(effect);
    }
}
