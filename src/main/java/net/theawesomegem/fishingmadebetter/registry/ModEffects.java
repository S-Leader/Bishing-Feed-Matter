package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectrifiedEffect;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Constants.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> ELECTRIFIED = EFFECTS.register("electrified", ElectrifiedEffect::new);

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
