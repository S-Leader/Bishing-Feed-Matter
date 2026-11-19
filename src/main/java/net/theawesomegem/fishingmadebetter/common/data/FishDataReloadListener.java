package net.theawesomegem.fishingmadebetter.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.nio.file.Path;
import java.util.Map;

public class FishDataReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final Identifier ID =
            Identifier.fromNamespaceAndPath("fishingmadebetter", "fish_data");
    private static final Gson GSON = new Gson();
    private final Path configDirectory;

    public FishDataReloadListener(Path configDirectory) {
        super(
                Codec.PASSTHROUGH.xmap(
                        value -> value.convert(JsonOps.INSTANCE).getValue(),
                        value -> new Dynamic<>(JsonOps.INSTANCE, value)),
                FileToIdConverter.json("fishingmadebetter/fish"));
        this.configDirectory = configDirectory;
    }

    @Override
    protected void apply(
            Map<Identifier, JsonElement> data,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        FishDataRegistry.reload(data, configDirectory);
    }
}
