package net.theawesomegem.fishingmadebetter.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class FishPopulationSavedData extends SavedData {
    private static final String DATA_NAME = "fishingmadebetter_populations";
    private static final SavedDataType<FishPopulationSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.parse(DATA_NAME),
                    FishPopulationSavedData::new,
                    CompoundTag.CODEC.xmap(
                            FishPopulationSavedData::load, data -> data.save(new CompoundTag())));
    private static final long MINUTE_TICKS = 60L * 20L;
    private final Map<Long, Map<String, PopulationData>> populationsByChunk = new LinkedHashMap<>();
    private final Set<Long> moreFishInitialized = new HashSet<>();

    public static FishPopulationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static FishPopulationSavedData load(CompoundTag tag) {
        FishPopulationSavedData data = new FishPopulationSavedData();
        for (long chunk : tag.getLongArray("MoreFishInitialized").orElseGet(() -> new long[0]))
            data.moreFishInitialized.add(chunk);
        ListTag chunks = tag.getListOrEmpty("Chunks");
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag chunkTag = chunks.getCompoundOrEmpty(i);
            long chunk = chunkTag.getLongOr("Chunk", 0L);
            Map<String, PopulationData> populations = new LinkedHashMap<>();
            ListTag fish = chunkTag.getListOrEmpty("Fish");
            for (int fishIndex = 0; fishIndex < fish.size(); fishIndex++) {
                CompoundTag fishTag = fish.getCompoundOrEmpty(fishIndex);
                PopulationData population =
                        new PopulationData(
                                fishTag.getStringOr("FishId", ""),
                                fishTag.getIntOr("Quantity", 0),
                                fishTag.getIntOr("ReproductionTick", 0),
                                fishTag.getLongOr("LastEatenTime", 0L));
                populations.put(population.fishId(), population);
            }
            data.populationsByChunk.put(chunk, populations);
        }
        return data;
    }

    public Collection<PopulationData> getPopulations(ChunkPos chunkPos) {
        return populationsByChunk.getOrDefault(chunkPos.pack(), Map.of()).values();
    }

    public Collection<PopulationData> getOrCreatePopulations(
            ServerLevel level, ChunkPos chunkPos, long gameTime) {
        generateInitialPopulations(level, chunkPos, gameTime);
        return getPopulations(chunkPos);
    }

    public void increasePopulation(ChunkPos chunkPos, String fishId, int amount, long gameTime) {
        Map<String, PopulationData> populations =
                populationsByChunk.computeIfAbsent(
                        chunkPos.pack(), ignored -> new LinkedHashMap<>());
        PopulationData population = populations.get(fishId);
        if (population == null) {
            populations.put(fishId, new PopulationData(fishId, amount, 0, gameTime));
        } else {
            populations.put(fishId, population.withQuantity(population.quantity() + amount));
        }
        setDirty();
    }

    public boolean feedFirstHungryPopulation(ChunkPos chunkPos, long gameTime) {
        return feedFirstHungryPopulation(chunkPos, gameTime, fishData -> true);
    }

    public boolean feedFirstHungryPopulation(
            ChunkPos chunkPos, long gameTime, Predicate<FishData> predicate) {
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.pack());
        if (populations == null) {
            return false;
        }

        for (PopulationData population : populations.values()) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null
                    || population.quantity() < 2
                    || !predicate.test(fishData)
                    || !population.isHungry(fishData, gameTime)) {
                continue;
            }
            populations.put(population.fishId(), population.withLastEatenTime(gameTime));
            setDirty();
            return true;
        }
        return false;
    }

    public boolean feedHungryPopulations(
            ChunkPos chunkPos, long gameTime, ToIntFunction<FishData> baitConsumer) {
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.pack());
        if (populations == null) {
            return false;
        }

        boolean updated = false;
        for (PopulationData population : populations.values().toArray(PopulationData[]::new)) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null
                    || population.quantity() < 2
                    || !population.isHungry(fishData, gameTime)) {
                continue;
            }

            int baitConsumed = baitConsumer.applyAsInt(fishData);
            if (baitConsumed <= 0) {
                continue;
            }

            populations.put(population.fishId(), population.withLastEatenTime(gameTime));
            updated = true;
        }

        if (updated) {
            setDirty();
        }
        return updated;
    }

    public boolean tickReproduction(ChunkPos chunkPos, long gameTime) {
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.pack());
        if (populations == null) {
            return false;
        }

        boolean updated = false;
        for (PopulationData population : populations.values().toArray(PopulationData[]::new)) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null
                    || fishData.reproductionTicks() <= 0
                    || population.quantity() < 2
                    || population.isHungry(fishData, gameTime)) {
                continue;
            }

            int reproductionTick = population.reproductionTick() + 1;
            int quantity = population.quantity();
            if (reproductionTick >= fishData.reproductionTicks()) {
                reproductionTick = 0;
                quantity++;
            }
            populations.put(
                    population.fishId(),
                    new PopulationData(
                            population.fishId(),
                            quantity,
                            reproductionTick,
                            population.lastEatenTime()));
            updated = true;
        }

        if (updated) {
            setDirty();
        }
        return updated;
    }

    @Nullable
    public PopulationData catchRandomFish(
            ServerLevel level, ChunkPos chunkPos, long gameTime, Predicate<FishData> predicate) {
        PopulationData selected = selectRandomFish(level, chunkPos, gameTime, predicate);
        if (selected == null) {
            return null;
        }
        return catchFish(chunkPos, selected.fishId()) ? selected : null;
    }

    @Nullable
    public PopulationData selectRandomFish(
            ServerLevel level, ChunkPos chunkPos, long gameTime, Predicate<FishData> predicate) {
        return selectRandomFish(
                level,
                chunkPos,
                gameTime,
                predicate,
                fishData -> {
                    Map<String, PopulationData> populations =
                            populationsByChunk.getOrDefault(chunkPos.pack(), Map.of());
                    PopulationData population = populations.get(fishData.fishId());
                    return population == null ? 0 : population.quantity();
                });
    }

    @Nullable
    public PopulationData selectRandomFish(
            ServerLevel level,
            ChunkPos chunkPos,
            long gameTime,
            Predicate<FishData> predicate,
            ToIntFunction<FishData> weightFunction) {
        generateInitialPopulations(level, chunkPos, gameTime);
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.pack());
        if (populations == null || populations.isEmpty()) {
            return null;
        }

        Map<String, Integer> weights = new LinkedHashMap<>();
        int totalWeight = 0;
        for (PopulationData population : populations.values()) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData != null
                    && population.quantity() > 0
                    && hasHabitat(level, chunkPos, fishData)
                    && predicate.test(fishData)) {
                int weight = Math.max(0, weightFunction.applyAsInt(fishData));
                weights.put(population.fishId(), weight);
                totalWeight += weight;
            }
        }
        if (totalWeight <= 0) {
            return null;
        }

        int selectedWeight = level.getRandom().nextInt(totalWeight);
        for (PopulationData population : populations.values()) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null
                    || population.quantity() <= 0
                    || !hasHabitat(level, chunkPos, fishData)
                    || !predicate.test(fishData)) {
                continue;
            }

            selectedWeight -= weights.getOrDefault(population.fishId(), 0);
            if (selectedWeight < 0) {
                return population;
            }
        }
        return null;
    }

    public boolean catchFish(ChunkPos chunkPos, String fishId) {
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.pack());
        if (populations == null) {
            return false;
        }

        PopulationData population = populations.get(fishId);
        if (population == null || population.quantity() <= 0) {
            return false;
        }

        int newQuantity = population.quantity() - 1;
        if (newQuantity <= 0) {
            populations.remove(fishId);
        } else {
            populations.put(fishId, population.withQuantity(newQuantity));
        }
        setDirty();
        return true;
    }

    public boolean generateInitialPopulations(ServerLevel level, ChunkPos chunkPos, long gameTime) {
        long chunkKey = chunkPos.pack();
        if (populationsByChunk.containsKey(chunkKey)) {
            if (moreFishInitialized.add(chunkKey)) {
                RandomSource random = RandomSource.create(level.getSeed() ^ chunkKey);
                Map<String, PopulationData> populations = populationsByChunk.get(chunkKey);
                for (FishData fish : FishDataRegistry.all()) {
                    if (fish.waterColumn()
                            && !populations.containsKey(fish.fishId())
                            && hasHabitat(level, chunkPos, fish)) {
                        int quantity = initialPopulation(random, fish);
                        if (quantity > 0)
                            populations.put(
                                    fish.fishId(),
                                    new PopulationData(fish.fishId(), quantity, 0, gameTime));
                    }
                }
                setDirty();
            }
            return false;
        }

        RandomSource random = RandomSource.create(level.getSeed() ^ chunkKey);
        Map<String, PopulationData> populations = new LinkedHashMap<>();
        for (FishData fishData : FishDataRegistry.all()) {
            if (!hasHabitat(level, chunkPos, fishData)) {
                continue;
            }
            int quantity = initialPopulation(random, fishData);
            if (quantity > 0) {
                populations.put(
                        fishData.fishId(),
                        new PopulationData(fishData.fishId(), quantity, 0, gameTime));
            }
        }

        populationsByChunk.put(chunkKey, populations);
        moreFishInitialized.add(chunkKey);
        setDirty();
        return !populations.isEmpty();
    }

    private static int initialPopulation(RandomSource random, FishData fishData) {
        float variance = 0.75F + random.nextFloat() * 0.5F;
        return Math.max(0, Math.round(fishData.rarity() * 0.5F * variance));
    }

    private static BlockPos centerOfChunk(ChunkPos chunkPos, FishData fishData) {
        int y = Math.max(-64, Math.min(320, (fishData.minYLevel() + fishData.maxYLevel()) / 2));
        return new BlockPos(chunkPos.getMiddleBlockX(), y, chunkPos.getMiddleBlockZ());
    }

    private static boolean hasHabitat(ServerLevel level, ChunkPos chunkPos, FishData fish) {
        if (!fish.waterColumn()) return fish.canExistAt(level, centerOfChunk(chunkPos, fish));
        BlockPos.MutableBlockPos pos =
                new BlockPos.MutableBlockPos(
                        chunkPos.getMiddleBlockX(), fish.maxYLevel(), chunkPos.getMiddleBlockZ());
        int minY = Math.max(level.getMinY(), fish.minYLevel());
        for (int y = Math.min(level.getMaxY() - 1, fish.maxYLevel()); y >= minY; y--) {
            pos.setY(y);
            if (level.getFluidState(pos).is(FluidTags.WATER) && fish.canExistAt(level, pos))
                return true;
        }
        return false;
    }

    public CompoundTag save(CompoundTag tag) {
        ListTag chunks = new ListTag();
        populationsByChunk.forEach(
                (chunk, populations) -> {
                    CompoundTag chunkTag = new CompoundTag();
                    chunkTag.putLong("Chunk", chunk);
                    ListTag fish = new ListTag();
                    populations
                            .values()
                            .forEach(
                                    population -> {
                                        CompoundTag fishTag = new CompoundTag();
                                        fishTag.putString("FishId", population.fishId());
                                        fishTag.putInt("Quantity", population.quantity());
                                        fishTag.putInt(
                                                "ReproductionTick", population.reproductionTick());
                                        fishTag.putLong(
                                                "LastEatenTime", population.lastEatenTime());
                                        fish.add(fishTag);
                                    });
                    chunkTag.put("Fish", fish);
                    chunks.add(chunkTag);
                });
        tag.put("Chunks", chunks);
        tag.putLongArray(
                "MoreFishInitialized",
                moreFishInitialized.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }

    public record PopulationData(
            String fishId, int quantity, int reproductionTick, long lastEatenTime) {
        private boolean isHungry(FishData fishData, long gameTime) {
            return gameTime - lastEatenTime > fishData.eatingFrequencyMinutes() * MINUTE_TICKS;
        }

        public PopulationData withQuantity(int newQuantity) {
            return new PopulationData(fishId, newQuantity, reproductionTick, lastEatenTime);
        }

        public PopulationData withLastEatenTime(long newLastEatenTime) {
            return new PopulationData(fishId, quantity, reproductionTick, newLastEatenTime);
        }
    }
}
