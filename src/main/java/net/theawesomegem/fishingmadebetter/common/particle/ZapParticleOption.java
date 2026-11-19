package net.theawesomegem.fishingmadebetter.common.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;

public record ZapParticleOption(Vec3 destination) implements ParticleOptions {
    public static final MapCodec<ZapParticleOption> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Vec3.CODEC.fieldOf("destination").forGetter(ZapParticleOption::destination)).apply(i, ZapParticleOption::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ZapParticleOption> STREAM_CODEC = StreamCodec.of((buffer, option) -> {
        buffer.writeDouble(option.destination.x);
        buffer.writeDouble(option.destination.y);
        buffer.writeDouble(option.destination.z);
    }, buffer -> new ZapParticleOption(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())));

    @Override
    public ParticleType<ZapParticleOption> getType() {
        return ModParticles.ZAP.get();
    }
}
