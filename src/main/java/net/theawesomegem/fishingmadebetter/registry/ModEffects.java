package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectrifiedEffect;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Constants.MOD_ID);

    public static final RegistryObject<MobEffect> ELECTRIFIED = EFFECTS.register("electrified", ElectrifiedEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
