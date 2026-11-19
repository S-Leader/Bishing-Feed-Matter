package net.theawesomegem.fishingmadebetter.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;

import java.util.Locale;

public record ZapParticleOption(Vec3 destination) implements ParticleOptions {
    public static final Codec<ZapParticleOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("destination").forGetter(ZapParticleOption::destination)
    ).apply(instance, ZapParticleOption::new));
    public static final Deserializer<ZapParticleOption> DESERIALIZER = new Deserializer<>() {
        @Override
        public ZapParticleOption fromCommand(ParticleType<ZapParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double x = reader.readDouble();
            reader.expect(' ');
            double y = reader.readDouble();
            reader.expect(' ');
            double z = reader.readDouble();
            return new ZapParticleOption(new Vec3(x, y, z));
        }

        @Override
        public ZapParticleOption fromNetwork(ParticleType<ZapParticleOption> type, FriendlyByteBuf buffer) {
            return new ZapParticleOption(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
    };

    @Override
    public ParticleType<ZapParticleOption> getType() {
        return ModParticles.ZAP.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeDouble(destination.x);
        buffer.writeDouble(destination.y);
        buffer.writeDouble(destination.z);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %s %s %s", ModParticles.ZAP.getId(), destination.x, destination.y, destination.z);
    }
}
