package net.theawesomegem.fishingmadebetter.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Reader;
import java.io.IOException;
import com.google.gson.Gson;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.data.FishData.FishingLiquid;
import net.theawesomegem.fishingmadebetter.common.data.FishData.TimeToFish;

public final class FishDataRegistry {
    private static final Map<String, FishData> FISH_DATA = new LinkedHashMap<>();
    private static final Gson GSON = new Gson();

    private FishDataRegistry() {
    }

    public static void init() {
        FISH_DATA.clear();
        registerDefaults();
        Constants.LOG.info("Loaded {} built-in fish data entries", FISH_DATA.size());
    }

    public static void reload(Map<ResourceLocation, JsonElement> jsonData) {
        reload(jsonData, null);
    }

    public static void reload(Map<ResourceLocation, JsonElement> jsonData, @Nullable Path configDirectory) {
        FISH_DATA.clear();
        registerDefaults();
        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonData.entrySet()) {
            try {
                loaded += parseDocument(entry.getKey(), entry.getValue());
            } catch (JsonSyntaxException | IllegalArgumentException exception) {
                Constants.LOG.warn("Skipping invalid fish data {}: {}", entry.getKey(), exception.getMessage());
            }
        }
        loaded += loadConfigDirectory(configDirectory);
        Constants.LOG.info("Loaded {} fish data entries ({} from datapacks/config)", FISH_DATA.size(), loaded);
    }

    @Nullable
    public static FishData get(String fishId) {
        return FISH_DATA.get(fishId);
    }

    public static Collection<FishData> all() {
        return FISH_DATA.values();
    }

    @Nullable
    public static Item resolveItem(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR ? null : item;
    }

    private static void registerDefaults() {
        register(defaultFish("Clownfish", "minecraft:tropical_fish", "a bright orange fish with white stripes and black edges", 600, 800, 3, 5, 1, 1, 15, 5, 25, 55, 80, 16, 9, 4, false, "", false, false, false, "", false, List.of("vegetable")));
        register(defaultFish("Pufferfish", "minecraft:pufferfish", "a small, yellow, thorny fish that inflates when threatened", 1200, 1600, 2, 4, 8, 14, 6, 10, 30, 55, 80, 16, 12, 5, true, "minecraft:arrow", true, true, true, "", true, List.of("meat_normal")));
        register(defaultFish("Cod", "minecraft:cod", "a large grey-green stout bodied fish with a large head and long chin barbel", 1600, 2000, 1, 3, 10, 40, 20, 40, 100, 55, 80, 16, 20, 8, false, "", false, true, true, "", true, List.of("meat_normal")));
        register(defaultFish("Red Salmon", "minecraft:salmon", "a decent sized red fish with a hooked jaw", 1000, 1400, 1, 4, 2, 8, 20, 10, 60, 55, 120, 16, 15, 6, false, "", false, true, true, "", true, List.of("meat_normal")));
    }

    private static int parseDocument(ResourceLocation id, JsonElement element) {
        JsonObject json = GsonHelper.convertToJsonObject(element, "fish data");
        if (json.has("item") || json.has("itemId")) {
            return register(parse(id, json)) ? 1 : 0;
        }

        int loaded = 0;
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            ResourceLocation childId = new ResourceLocation(id.getNamespace(), id.getPath() + "/" + sanitizePath(entry.getKey()));
            JsonObject child = entry.getValue().getAsJsonObject();
            if (!child.has("fishId") && !child.has("fish_id")) {
                child.addProperty("fishId", entry.getKey());
            }
            if (register(parse(childId, child))) {
                loaded++;
            }
        }
        return loaded;
    }

    private static int loadConfigDirectory(@Nullable Path configDirectory) {
        if (configDirectory == null || !Files.isDirectory(configDirectory)) {
            return 0;
        }
        int loaded = 0;
        try (var paths = Files.walk(configDirectory)) {
            for (Path path : paths.filter(file -> Files.isRegularFile(file) && file.getFileName().toString().endsWith(".json")).sorted().toList()) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    JsonElement json = GSON.fromJson(reader, JsonElement.class);
                    loaded += parseDocument(new ResourceLocation(Constants.MOD_ID, "config/" + sanitizePath(path.getFileName().toString())), json);
                } catch (RuntimeException | IOException exception) {
                    Constants.LOG.warn("Skipping invalid legacy fish config {}: {}", path, exception.getMessage());
                }
            }
        } catch (IOException exception) {
            Constants.LOG.warn("Unable to read fish config directory {}: {}", configDirectory, exception.getMessage());
        }
        return loaded;
    }

    private static String sanitizePath(String value) {
        String path = value.toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
        return path.endsWith(".json") ? path.substring(0, path.length() - 5) : path;
    }

    private static FishData parse(ResourceLocation id, JsonObject json) {
        String fishId = string(json, "fish_id", "fishId", id.toString());
        String itemId = string(json, "item", "itemId", "");
        if (itemId.isEmpty()) {
            throw new JsonSyntaxException("Missing fish item/itemId");
        }
        String description = GsonHelper.getAsString(json, "description", "");
        int minFishTime = integer(json, "min_fish_time", "minFishTime", 600);
        int maxFishTime = integer(json, "max_fish_time", "maxFishTime", 1200);
        int minErrorVariance = integer(json, "min_error_variance", "minErrorVariance", 1);
        int maxErrorVariance = integer(json, "max_error_variance", "maxErrorVariance", 5);
        int minWeight = integer(json, "min_weight", "minWeight", 1);
        int maxWeight = integer(json, "max_weight", "maxWeight", 1);
        TimeToFish timeToFish = TimeToFish.valueOf(string(json, "time_to_fish", "time", TimeToFish.ANY.name()).toUpperCase());
        FishingLiquid liquid = FishingLiquid.valueOf(GsonHelper.getAsString(json, "liquid", FishingLiquid.WATER.name()).toUpperCase());
        boolean rainRequired = bool(json, "rain_required", "rainRequired", false);
        boolean thunderRequired = bool(json, "thunder_required", "thunderRequired", false);
        int rarity = GsonHelper.getAsInt(json, "rarity", 10);
        int minDeepLevel = integer(json, "min_depth", "minDeepLevel", 0);
        int maxDeepLevel = integer(json, "max_depth", "maxDeepLevel", 320);
        int minYLevel = integer(json, "min_y", "minYLevel", 0);
        int maxYLevel = integer(json, "max_y", "maxYLevel", 320);
        int maxLightLevel = integer(json, "max_light", "maxLightLevel", 15);
        int reproductionTicks = integer(json, "reproduction_ticks", "reproductionTime", 20);
        int eatingFrequencyMinutes = integer(json, "eating_frequency_minutes", "eatingFrequency", 8);
        boolean trackable = GsonHelper.getAsBoolean(json, "trackable", true);
        boolean biomeListBlacklist = bool(json, "biome_list_blacklist", "biomeBlacklist", false);
        boolean dimensionListBlacklist = bool(json, "dimension_list_blacklist", "dimensionBlacklist", false);
        List<String> biomeList = readStringList(json, "biomes", "biomeTagList");
        List<String> dimensionList = readStringList(json, "dimensions", "dimensionList");
        int timeAliveOutsideWater = integer(json, "time_alive_outside_water", "timeOutsideOfWater", 20);
        boolean allowScaling = bool(json, "allow_scaling", "allowScaling", false);
        String scalingItem = string(json, "scaling_item", "scalingItem", "");
        boolean scalingUseWeight = bool(json, "scaling_use_weight", "scalingUseWeight", false);
        boolean allowFillet = bool(json, "allow_fillet", "allowFillet", true);
        boolean defaultFillet = bool(json, "default_fillet", "defaultFillet", true);
        String filletItem = string(json, "fillet_item", "filletItem", "");
        boolean filletUseWeight = bool(json, "fillet_use_weight", "filletUseWeight", true);
        List<String> validBaits = readBaits(json);

        return new FishData(
                fishId,
                itemId,
                description,
                minFishTime,
                maxFishTime,
                minErrorVariance,
                maxErrorVariance,
                minWeight,
                maxWeight,
                timeToFish,
                liquid,
                rainRequired,
                thunderRequired,
                rarity,
                minDeepLevel,
                maxDeepLevel,
                minYLevel,
                maxYLevel,
                maxLightLevel,
                reproductionTicks,
                eatingFrequencyMinutes,
                trackable,
                biomeListBlacklist,
                dimensionListBlacklist,
                biomeList,
                dimensionList,
                timeAliveOutsideWater,
                allowScaling,
                scalingItem,
                scalingUseWeight,
                allowFillet,
                defaultFillet,
                filletItem,
                filletUseWeight,
                validBaits
        );
    }

    private static FishData defaultFish(String fishId, String itemId, String description, int minFishTime, int maxFishTime, int minErrorVariance, int maxErrorVariance, int minWeight, int maxWeight, int rarity, int minDeepLevel, int maxDeepLevel, int minYLevel, int maxYLevel, int maxLightLevel, int reproductionTicks, int eatingFrequencyMinutes, boolean allowScaling, String scalingItem, boolean scalingUseWeight, boolean allowFillet, boolean defaultFillet, String filletItem, boolean filletUseWeight, List<String> validBaits) {
        return new FishData(
                fishId,
                itemId,
                description,
                minFishTime,
                maxFishTime,
                minErrorVariance,
                maxErrorVariance,
                minWeight,
                maxWeight,
                TimeToFish.ANY,
                FishingLiquid.WATER,
                false,
                false,
                rarity,
                minDeepLevel,
                maxDeepLevel,
                minYLevel,
                maxYLevel,
                maxLightLevel,
                reproductionTicks,
                eatingFrequencyMinutes,
                true,
                false,
                true,
                List.of("minecraft:ocean", "minecraft:beach", "minecraft:river", "minecraft:mountains", "minecraft:plains", "minecraft:forest", "minecraft:swamp", "minecraft:cold_ocean"),
                List.of("minecraft:the_nether", "minecraft:the_end"),
                20,
                allowScaling,
                scalingItem,
                scalingUseWeight,
                allowFillet,
                defaultFillet,
                filletItem,
                filletUseWeight,
                validBaits
        );
    }

    private static String string(JsonObject json, String modernKey, String legacyKey, String defaultValue) {
        return json.has(modernKey) ? GsonHelper.getAsString(json, modernKey) : GsonHelper.getAsString(json, legacyKey, defaultValue);
    }

    private static int integer(JsonObject json, String modernKey, String legacyKey, int defaultValue) {
        return json.has(modernKey) ? GsonHelper.getAsInt(json, modernKey) : GsonHelper.getAsInt(json, legacyKey, defaultValue);
    }

    private static boolean bool(JsonObject json, String modernKey, String legacyKey, boolean defaultValue) {
        return json.has(modernKey) ? GsonHelper.getAsBoolean(json, modernKey) : GsonHelper.getAsBoolean(json, legacyKey, defaultValue);
    }

    private static List<String> readBaits(JsonObject json) {
        List<String> values = readStringList(json, "valid_baits", "validBaits");
        if (!values.isEmpty() || !json.has("baitItemMap")) {
            return values;
        }
        return List.copyOf(GsonHelper.getAsJsonObject(json, "baitItemMap").keySet());
    }

    private static List<String> readStringList(JsonObject json, String modernKey, String legacyKey) {
        String key = json.has(modernKey) ? modernKey : legacyKey;
        if (!json.has(key)) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(json, key);
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return List.copyOf(values);
    }

    private static boolean register(FishData fishData) {
        if (resolveItem(fishData.itemId()) == null) {
            Constants.LOG.warn("Skipping fish data entry with missing item: {}", fishData.itemId());
            return false;
        }
        if (!fishData.scalingItem().isEmpty() && resolveItem(fishData.scalingItem()) == null) {
            Constants.LOG.warn("Skipping fish data entry with missing scaling item: {}", fishData.scalingItem());
            return false;
        }
        if (!fishData.filletItem().isEmpty() && resolveItem(fishData.filletItem()) == null) {
            Constants.LOG.warn("Skipping fish data entry with missing fillet item: {}", fishData.filletItem());
            return false;
        }
        FISH_DATA.put(fishData.fishId(), fishData);
        return true;
    }
}
