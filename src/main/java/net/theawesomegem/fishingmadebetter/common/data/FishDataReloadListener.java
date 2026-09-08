package net.theawesomegem.fishingmadebetter.common.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.nio.file.Path;

public class FishDataReloadListener extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation ID = new ResourceLocation("fishingmadebetter", "fish_data");
    private static final Gson GSON = new Gson();
    private final Path configDirectory;

    public FishDataReloadListener(Path configDirectory) {
        super(GSON, "fishingmadebetter/fish");
        this.configDirectory = configDirectory;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, net.minecraft.server.packs.resources.ResourceManager resourceManager, ProfilerFiller profiler) {
        FishDataRegistry.reload(data, configDirectory);
    }
}
