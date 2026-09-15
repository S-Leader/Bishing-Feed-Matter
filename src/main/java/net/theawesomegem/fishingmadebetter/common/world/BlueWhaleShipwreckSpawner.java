package net.theawesomegem.fishingmadebetter.common.world;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.BlueWhaleEntity;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

public final class BlueWhaleShipwreckSpawner {
    private static final ResourceLocation SHIPWRECK_ID = new ResourceLocation("minecraft", "shipwreck");
    private static final Map<ServerLevel, Queue<Long>> PENDING = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<ServerLevel, Set<Long>> QUEUED = Collections.synchronizedMap(new WeakHashMap<>());

    private BlueWhaleShipwreckSpawner() {
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        boolean hasShipwreckStart = event.getChunk().getAllStarts().keySet().stream()
                .anyMatch(structure -> SHIPWRECK_ID.equals(structures.getKey(structure)));
        if (!hasShipwreckStart) {
            return;
        }
        long chunk = event.getChunk().getPos().toLong();
        Set<Long> queued = QUEUED.computeIfAbsent(level, ignored -> new java.util.HashSet<>());
        if (queued.add(chunk)) {
            PENDING.computeIfAbsent(level, ignored -> new ArrayDeque<>()).add(chunk);
        }
    }

    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        Queue<Long> pending = PENDING.get(level);
        if (pending == null) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            Long packedChunk = pending.poll();
            if (packedChunk == null) {
                break;
            }
            Set<Long> queued = QUEUED.get(level);
            if (queued != null) {
                queued.remove(packedChunk);
            }
            inspectChunk(level, new ChunkPos(packedChunk));
        }
        if (pending.isEmpty()) {
            PENDING.remove(level);
        }
    }

    private static void inspectChunk(ServerLevel level, ChunkPos chunkPos) {
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure shipwreck = structures.get(SHIPWRECK_ID);
        if (shipwreck == null) {
            return;
        }
        StructureStart start = chunk.getStartForStructure(shipwreck);
        if (start == null || !start.isValid()) {
            return;
        }

        long startChunk = start.getChunkPos().toLong();
        BlueWhaleShipwreckSavedData savedData = BlueWhaleShipwreckSavedData.get(level);
        if (savedData.hasChecked(startChunk)) {
            return;
        }

        BoundingBox box = start.getBoundingBox();
        BlockPos biomePosition = new BlockPos(box.getCenter().getX(), Math.min(level.getSeaLevel() - 1, box.maxY()), box.getCenter().getZ());
        Holder<Biome> biome = level.getBiome(biomePosition);
        if (!biome.is(BiomeTags.IS_OCEAN) || !biome.is(Tags.Biomes.IS_COLD_OVERWORLD)) {
            savedData.markChecked(startChunk);
            return;
        }
        if (!isFullySubmerged(level, box)) {
            savedData.markChecked(startChunk);
            return;
        }

        savedData.markChecked(startChunk);
        RandomSource structureRandom = RandomSource.create(level.getSeed() ^ startChunk * 0x9E3779B97F4A7C15L ^ 0x42574C5545574841L);
        if (structureRandom.nextFloat() >= 0.1F) {
            return;
        }

        BlueWhaleEntity whale = ModEntities.BLUE_WHALE.get().create(level);
        if (whale == null) {
            return;
        }
        float spawnYaw = structureRandom.nextFloat() * 360.0F;
        Vec3 spawnPosition = findSpawnPosition(level, box, whale, structureRandom, spawnYaw);
        if (spawnPosition == null) {
            Constants.LOG.debug("A blue whale passed its shipwreck roll at {}, but the wreck had no safe water volume", box.getCenter());
            return;
        }

        whale.moveTo(spawnPosition.x, spawnPosition.y, spawnPosition.z, spawnYaw, 0.0F);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(whale.blockPosition());
        whale.finalizeSpawn(level, difficulty, MobSpawnType.STRUCTURE, null, null);
        whale.setAirSupply(whale.getMaxAirSupply());
        whale.setHealth(whale.getMaxHealth());
        whale.setPersistenceRequired();
        level.addFreshEntity(whale);
    }

    private static boolean isFullySubmerged(ServerLevel level, BoundingBox box) {
        int waterY = box.maxY() + 1;
        if (waterY + 2 >= level.getMaxBuildHeight()) {
            return false;
        }
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                if (!level.getFluidState(new BlockPos(x, waterY, z)).is(FluidTags.WATER)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Vec3 findSpawnPosition(ServerLevel level, BoundingBox box, BlueWhaleEntity whale, RandomSource random, float yaw) {
        BlockPos center = box.getCenter();
        int baseY = box.maxY() + 1;
        for (int attempt = 0; attempt < 36; attempt++) {
            int radius = attempt == 0 ? 0 : 2 + attempt / 4;
            int x = center.getX() + (radius == 0 ? 0 : random.nextInt(-radius, radius + 1));
            int z = center.getZ() + (radius == 0 ? 0 : random.nextInt(-radius, radius + 1));
            int y = baseY + random.nextInt(0, 3);
            if (!hasWhaleSizedWater(level, x, y, z)) {
                continue;
            }
            whale.moveTo(x + 0.5D, y + 0.1D, z + 0.5D, yaw, 0.0F);
            whale.yBodyRot = yaw;
            whale.refreshBodyPartsForSpawn();
            boolean clearParts = java.util.Arrays.stream(whale.getParts()).allMatch(level::noCollision);
            if (level.noCollision(whale) && clearParts) {
                return whale.position();
            }
        }
        return null;
    }

    private static boolean hasWhaleSizedWater(ServerLevel level, int centerX, int bottomY, int centerZ) {
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                for (int y = bottomY; y <= bottomY + 2; y++) {
                    if (!level.getFluidState(new BlockPos(x, y, z)).is(FluidTags.WATER)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
