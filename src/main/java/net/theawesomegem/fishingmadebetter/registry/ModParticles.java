package net.theawesomegem.fishingmadebetter.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.particle.ZapParticleOption;

public final class ModParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Constants.MOD_ID);
    public static final RegistryObject<ParticleType<ZapParticleOption>> ZAP = PARTICLES.register("zap", () -> new ParticleType<>(false, ZapParticleOption.DESERIALIZER) {
        @Override
        public Codec<ZapParticleOption> codec() {
            return ZapParticleOption.CODEC;
        }
    });
    public static final RegistryObject<SimpleParticleType> SPARK = PARTICLES.register("spark_particle", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AIR_BUBBLE = PARTICLES.register("air_bubble", () -> new SimpleParticleType(false));

    private ModParticles() {
    }

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}
