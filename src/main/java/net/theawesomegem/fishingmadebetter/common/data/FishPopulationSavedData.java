package net.theawesomegem.fishingmadebetter.common.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public class FishPopulationSavedData extends SavedData {
    private static final String DATA_NAME = "fishingmadebetter_populations";
    private static final long MINUTE_TICKS = 60L * 20L;
    private final Map<Long, Map<String, PopulationData>> populationsByChunk = new LinkedHashMap<>();

    public static FishPopulationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FishPopulationSavedData::load, FishPopulationSavedData::new, DATA_NAME);
    }

    public static FishPopulationSavedData load(CompoundTag tag) {
        FishPopulationSavedData data = new FishPopulationSavedData();
        ListTag chunks = tag.getList("Chunks", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag chunkTag = chunks.getCompound(i);
            long chunk = chunkTag.getLong("Chunk");
            Map<String, PopulationData> populations = new LinkedHashMap<>();
            ListTag fish = chunkTag.getList("Fish", CompoundTag.TAG_COMPOUND);
            for (int fishIndex = 0; fishIndex < fish.size(); fishIndex++) {
                CompoundTag fishTag = fish.getCompound(fishIndex);
                PopulationData population = new PopulationData(
                        fishTag.getString("FishId"),
                        fishTag.getInt("Quantity"),
                        fishTag.getInt("ReproductionTick"),
                        fishTag.getLong("LastEatenTime")
                );
                populations.put(population.fishId(), population);
            }
            if (!populations.isEmpty()) {
                data.populationsByChunk.put(chunk, populations);
            }
        }
        return data;
    }

    public Collection<PopulationData> getPopulations(ChunkPos chunkPos) {
        return populationsByChunk.getOrDefault(chunkPos.toLong(), Map.of()).values();
    }

    public Collection<PopulationData> getOrCreatePopulations(ServerLevel level, ChunkPos chunkPos, long gameTime) {
        generateInitialPopulations(level, chunkPos, gameTime);
        return getPopulations(chunkPos);
    }

    public void increasePopulation(ChunkPos chunkPos, String fishId, int amount, long gameTime) {
        Map<String, PopulationData> populations = populationsByChunk.computeIfAbsent(chunkPos.toLong(), ignored -> new LinkedHashMap<>());
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

    public boolean feedFirstHungryPopulation(ChunkPos chunkPos, long gameTime, Predicate<FishData> predicate) {
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.toLong());
        if (populations == null) {
            return false;
        }

        for (PopulationData population : populations.values()) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null || population.quantity() < 2 || !predicate.test(fishData) || !population.isHungry(fishData, gameTime)) {
                continue;
            }
            populations.put(population.fishId(), population.withLastEatenTime(gameTime));
            setDirty();
            return true;
        }
        return false;
    }

    public boolean feedHungryPopulations(ChunkPos chunkPos, long gameTime, ToIntFunction<FishData> baitConsumer) {
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.toLong());
        if (populations == null) {
            return false;
        }

        boolean updated = false;
        for (PopulationData population : populations.values().toArray(PopulationData[]::new)) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null || population.quantity() < 2 || !population.isHungry(fishData, gameTime)) {
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
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.toLong());
        if (populations == null) {
            return false;
        }

        boolean updated = false;
        for (PopulationData population : populations.values().toArray(PopulationData[]::new)) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null || fishData.reproductionTicks() <= 0 || population.quantity() < 2 || population.isHungry(fishData, gameTime)) {
                continue;
            }

            int reproductionTick = population.reproductionTick() + 1;
            int quantity = population.quantity();
            if (reproductionTick >= fishData.reproductionTicks()) {
                reproductionTick = 0;
                quantity++;
            }
            populations.put(population.fishId(), new PopulationData(population.fishId(), quantity, reproductionTick, population.lastEatenTime()));
            updated = true;
        }

        if (updated) {
            setDirty();
        }
        return updated;
    }

    @Nullable
    public PopulationData catchRandomFish(ServerLevel level, ChunkPos chunkPos, long gameTime, Predicate<FishData> predicate) {
        PopulationData selected = selectRandomFish(level, chunkPos, gameTime, predicate);
        if (selected == null) {
            return null;
        }
        return catchFish(chunkPos, selected.fishId()) ? selected : null;
    }

    @Nullable
    public PopulationData selectRandomFish(ServerLevel level, ChunkPos chunkPos, long gameTime, Predicate<FishData> predicate) {
        return selectRandomFish(level, chunkPos, gameTime, predicate, fishData -> {
            Map<String, PopulationData> populations = populationsByChunk.getOrDefault(chunkPos.toLong(), Map.of());
            PopulationData population = populations.get(fishData.fishId());
            return population == null ? 0 : population.quantity();
        });
    }

    @Nullable
    public PopulationData selectRandomFish(ServerLevel level, ChunkPos chunkPos, long gameTime, Predicate<FishData> predicate, ToIntFunction<FishData> weightFunction) {
        generateInitialPopulations(level, chunkPos, gameTime);
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.toLong());
        if (populations == null || populations.isEmpty()) {
            return null;
        }

        Map<String, Integer> weights = new LinkedHashMap<>();
        int totalWeight = 0;
        for (PopulationData population : populations.values()) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData != null && population.quantity() > 0 && fishData.canExistAt(level, centerOfChunk(chunkPos, fishData)) && predicate.test(fishData)) {
                int weight = Math.max(0, weightFunction.applyAsInt(fishData));
                weights.put(population.fishId(), weight);
                totalWeight += weight;
            }
        }
        if (totalWeight <= 0) {
            return null;
        }

        int selectedWeight = level.random.nextInt(totalWeight);
        for (PopulationData population : populations.values()) {
            FishData fishData = FishDataRegistry.get(population.fishId());
            if (fishData == null || population.quantity() <= 0 || !fishData.canExistAt(level, centerOfChunk(chunkPos, fishData)) || !predicate.test(fishData)) {
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
        Map<String, PopulationData> populations = populationsByChunk.get(chunkPos.toLong());
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
        long chunkKey = chunkPos.toLong();
        if (populationsByChunk.containsKey(chunkKey)) {
            return false;
        }

        RandomSource random = RandomSource.create(level.getSeed() ^ chunkKey);
        Map<String, PopulationData> populations = new LinkedHashMap<>();
        for (FishData fishData : FishDataRegistry.all()) {
            if (!fishData.canExistAt(level, centerOfChunk(chunkPos, fishData))) {
                continue;
            }
            int quantity = initialPopulation(random, fishData);
            if (quantity > 0) {
                populations.put(fishData.fishId(), new PopulationData(fishData.fishId(), quantity, 0, gameTime));
            }
        }

        populationsByChunk.put(chunkKey, populations);
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

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag chunks = new ListTag();
        populationsByChunk.forEach((chunk, populations) -> {
            if (populations.isEmpty()) {
                return;
            }

            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putLong("Chunk", chunk);
            ListTag fish = new ListTag();
            populations.values().forEach(population -> {
                CompoundTag fishTag = new CompoundTag();
                fishTag.putString("FishId", population.fishId());
                fishTag.putInt("Quantity", population.quantity());
                fishTag.putInt("ReproductionTick", population.reproductionTick());
                fishTag.putLong("LastEatenTime", population.lastEatenTime());
                fish.add(fishTag);
            });
            chunkTag.put("Fish", fish);
            chunks.add(chunkTag);
        });
        tag.put("Chunks", chunks);
        return tag;
    }

    public record PopulationData(String fishId, int quantity, int reproductionTick, long lastEatenTime) {
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
