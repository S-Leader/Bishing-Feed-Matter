package net.theawesomegem.fishingmadebetter.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.particle.ZapParticleOption;

public final class ModParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Constants.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, ParticleType<ZapParticleOption>> ZAP = PARTICLES.register("zap", () -> new ParticleType<ZapParticleOption>(false) {
        @Override
        public MapCodec<ZapParticleOption> codec() {
            return ZapParticleOption.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ZapParticleOption> streamCodec() {
            return ZapParticleOption.STREAM_CODEC;
        }
    });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK = PARTICLES.register("spark_particle", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AIR_BUBBLE = PARTICLES.register("air_bubble", () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}
