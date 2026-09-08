package net.theawesomegem.fishingmadebetter.common.data;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

public record FishData(
        String fishId,
        String itemId,
        String description,
        int minFishTime,
        int maxFishTime,
        int minErrorVariance,
        int maxErrorVariance,
        int minWeight,
        int maxWeight,
        TimeToFish timeToFish,
        FishingLiquid liquid,
        boolean rainRequired,
        boolean thunderRequired,
        int rarity,
        int minDeepLevel,
        int maxDeepLevel,
        int minYLevel,
        int maxYLevel,
        int maxLightLevel,
        int reproductionTicks,
        int eatingFrequencyMinutes,
        boolean trackable,
        boolean biomeListBlacklist,
        boolean dimensionListBlacklist,
        List<String> biomeList,
        List<String> dimensionList,
        int timeAliveOutsideWater,
        boolean allowScaling,
        String scalingItem,
        boolean scalingUseWeight,
        boolean allowFillet,
        boolean defaultFillet,
        String filletItem,
        boolean filletUseWeight,
        List<String> validBaits
) {
    public boolean canExistAt(ServerLevel level, BlockPos pos) {
        return isAltitudeValid(pos.getY())
                && isLightValid(level, pos)
                && isWeatherValid(level)
                && isTimeValid(level)
                && isDimensionValid(level)
                && isBiomeValid(level, pos);
    }

    public boolean isAltitudeValid(int y) {
        return y >= minYLevel && y <= maxYLevel;
    }

    public boolean isDepthValid(int depth) {
        return depth >= minDeepLevel && depth <= maxDeepLevel;
    }

    public boolean isBaitValid(String baitId) {
        return validBaits.isEmpty() || validBaits.contains(baitId);
    }

    private boolean isLightValid(ServerLevel level, BlockPos pos) {
        return level.getMaxLocalRawBrightness(pos) <= maxLightLevel;
    }

    private boolean isWeatherValid(ServerLevel level) {
        return (!rainRequired || level.isRaining()) && (!thunderRequired || level.isThundering());
    }

    private boolean isTimeValid(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;
        return switch (timeToFish) {
            case ANY -> true;
            case DAY -> dayTime < 12000L;
            case NIGHT -> dayTime >= 12000L;
        };
    }

    private boolean isDimensionValid(ServerLevel level) {
        if (dimensionList.isEmpty()) {
            return true;
        }
        boolean contains = dimensionList.stream().anyMatch(entry -> matchesDimension(level, entry));
        return dimensionListBlacklist != contains;
    }

    public static boolean matchesDimension(ServerLevel level, String entry) {
        String normalized = entry.trim();
        String dimension = level.dimension().location().toString();
        String dimensionPath = level.dimension().location().getPath();
        return normalized.equals(dimension)
                || normalized.equals(dimensionPath)
                || legacyDimensionId(level).equals(normalized);
    }

    private static String legacyDimensionId(ServerLevel level) {
        if (level.dimension() == ServerLevel.OVERWORLD) {
            return "0";
        }
        if (level.dimension() == ServerLevel.NETHER) {
            return "-1";
        }
        if (level.dimension() == ServerLevel.END) {
            return "1";
        }
        return level.dimension().location().toString();
    }

    private boolean isBiomeValid(ServerLevel level, BlockPos pos) {
        if (biomeList.isEmpty()) {
            return true;
        }

        Holder<Biome> biome = level.getBiome(pos);
        boolean contains = biomeList.stream().anyMatch(entry -> matchesBiome(biome, entry));
        return biomeListBlacklist != contains;
    }

    public static boolean matchesBiome(Holder<Biome> biome, String entry) {
        String normalized = entry.trim().toLowerCase();
        String biomeId = biome.unwrapKey().map(key -> key.location().toString()).orElse("");
        String biomePath = ResourceLocation.tryParse(biomeId) != null ? ResourceLocation.tryParse(biomeId).getPath() : biomeId;
        if (normalized.equals(biomeId) || normalized.equals(biomePath)) {
            return true;
        }

        return switch (normalized) {
            case "beach" -> biome.is(BiomeTags.IS_BEACH);
            case "river" -> biome.is(BiomeTags.IS_RIVER);
            case "ocean" -> biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN);
            case "mountain" -> biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL);
            case "forest" -> biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA);
            case "jungle" -> biome.is(BiomeTags.IS_JUNGLE);
            case "cold", "snowy" -> biome.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)
                    || biome.is(BiomeTags.SPAWNS_WHITE_RABBITS)
                    || biomePath.contains("cold")
                    || biomePath.contains("frozen")
                    || biomePath.contains("snow")
                    || biomePath.contains("ice");
            case "hot", "dry", "sandy" -> biome.is(BiomeTags.IS_BADLANDS)
                    || biome.is(BiomeTags.IS_SAVANNA)
                    || biomePath.contains("desert");
            case "swamp", "wet" -> biomePath.contains("swamp") || biomePath.contains("mangrove");
            case "mushroom" -> biomePath.contains("mushroom");
            case "dead", "spooky" -> biomePath.contains("dark_forest")
                    || biomePath.contains("deep_dark")
                    || biomePath.contains("soul");
            case "plains" -> biomePath.contains("plains") || biomePath.contains("meadow");
            case "nether" -> biome.is(BiomeTags.IS_NETHER);
            case "end" -> biome.is(BiomeTags.IS_END);
            default -> false;
        };
    }

    public enum TimeToFish {
        ANY,
        DAY,
        NIGHT
    }

    public enum FishingLiquid {
        WATER,
        LAVA,
        VOID,
        ANY
    }
}
