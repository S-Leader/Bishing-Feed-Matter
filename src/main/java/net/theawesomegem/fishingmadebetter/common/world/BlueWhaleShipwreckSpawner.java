package net.theawesomegem.fishingmadebetter.common.world;

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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.config.FmbCommonConfig;
import net.theawesomegem.fishingmadebetter.common.entity.BlueWhaleEntity;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

import java.util.*;

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
        boolean hasShipwreckStart = event.getChunk().getAllStarts().keySet().stream().anyMatch(structure -> SHIPWRECK_ID.equals(structures.getKey(structure)));
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
        if (!isFullySubmerged(level, start)) {
            savedData.markChecked(startChunk);
            return;
        }

        savedData.markChecked(startChunk);
        RandomSource structureRandom = RandomSource.create(level.getSeed() ^ startChunk * 0x9E3779B97F4A7C15L ^ 0x42574C5545574841L);
        if (structureRandom.nextDouble() >= FmbCommonConfig.whaleSpawnChance()) {
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

    private static boolean isActualShipBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }

        if (state.is(Blocks.WATER) || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT) || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)) {
            return false;
        }

        return true;
    }

    private static int findActualShipTopY(
            ServerLevel level,
            StructureStart start
    ) {
        int highest = Integer.MIN_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (StructurePiece piece : start.getPieces()) {
            BoundingBox box = piece.getBoundingBox();

            for (int y = box.maxY(); y >= box.minY(); y--) {
                boolean foundAtThisY = false;

                for (int x = box.minX(); x <= box.maxX(); x++) {
                    for (int z = box.minZ(); z <= box.maxZ(); z++) {
                        pos.set(x, y, z);

                        BlockState state = level.getBlockState(pos);

                        if (isActualShipBlock(state)) {
                            highest = Math.max(highest, y);
                            foundAtThisY = true;
                        }
                    }
                }

                if (foundAtThisY) {
                    break;
                }
            }
        }

        return highest;
    }

    private static boolean isFullySubmerged(ServerLevel level, StructureStart start) {
        int actualTopY = findActualShipTopY(level, start);

        if (actualTopY == Integer.MIN_VALUE) {
            return false;
        }

        int seaTop = level.getSeaLevel() - 1;

        if (actualTopY >= seaTop) {
            return false;
        }

        BoundingBox box = start.getBoundingBox();

        int centerX = (box.minX() + box.maxX()) >> 1;
        int centerZ = (box.minZ() + box.maxZ()) >> 1;

        int quarterX = Math.max(1, box.getXSpan() / 4);
        int quarterZ = Math.max(1, box.getZSpan() / 4);

        int[][] samples = {
                {centerX, centerZ},
                {centerX - quarterX, centerZ},
                {centerX + quarterX, centerZ},
                {centerX, centerZ - quarterZ},
                {centerX, centerZ + quarterZ},
                {centerX - quarterX, centerZ - quarterZ},
                {centerX - quarterX, centerZ + quarterZ},
                {centerX + quarterX, centerZ - quarterZ},
                {centerX + quarterX, centerZ + quarterZ}
        };

        int submergedColumns = 0;

        int firstAboveWreck = actualTopY + 1;

        for (int[] sample : samples) {
            if (hasWaterOrIceCover(
                    level,
                    sample[0],
                    sample[1],
                    firstAboveWreck,
                    seaTop
            )) {
                submergedColumns++;
            }
        }

        return submergedColumns >= 5;
    }

    private static boolean hasWaterOrIceCover(ServerLevel level, int x, int z, int minY, int maxY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, minY, z);
        for (int y = minY; y <= maxY; y++) {
            pos.setY(y);
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                continue;
            }
            var state = level.getBlockState(pos);
            if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static Vec3 findSpawnPosition(ServerLevel level, BoundingBox box, BlueWhaleEntity whale, RandomSource random, float yaw) {
        BlockPos center = box.getCenter();

        // The shipwreck is only the spawn anchor.  Do not try to place the whale on top of it.
        // Search an annulus around the wreck so the large multipart whale can find open ocean.
        int halfSpan = Math.max(box.getXSpan(), box.getZSpan()) / 2;
        int minRadius = Math.max(12, halfSpan + 8);
        int maxRadius = minRadius + 40;

        for (int attempt = 0; attempt < 128; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int radius = random.nextInt(minRadius, maxRadius + 1);
            int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);

            // Keep the actual whale in the same kind of ocean as the qualifying wreck.
            Holder<Biome> candidateBiome = level.getBiome(new BlockPos(x, Math.min(level.getSeaLevel() - 1, box.maxY()), z));
            if (!candidateBiome.is(BiomeTags.IS_OCEAN) || !candidateBiome.is(Tags.Biomes.IS_COLD_OVERWORLD)) {
                continue;
            }

            Integer y = findWaterColumnSpawnY(level, x, z, random);
            if (y == null) {
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

    private static Integer findWaterColumnSpawnY(ServerLevel level, int centerX, int centerZ, RandomSource random) {
        // Start below the ocean surface, then walk downward through the local water column.
        // This deliberately has no dependency on the shipwreck's box.maxY().
        int topY = Math.min(level.getSeaLevel() - 4, level.getMaxBuildHeight() - 5);
        int bottomY = Math.max(level.getMinBuildHeight() + 2, level.getSeaLevel() - 48);

        // Offset the first probe a little so whales do not all appear at exactly the same depth.
        int startY = Math.max(bottomY, topY - random.nextInt(0, 8));
        for (int y = startY; y >= bottomY; y--) {
            if (hasWhaleSizedWater(level, centerX, y, centerZ)) {
                return y;
            }
        }
        return null;
    }

    private static boolean hasWhaleSizedWater(ServerLevel level, int centerX, int bottomY, int centerZ) {
        // Require a useful local water pocket around the body. Kelp/seagrass still count because
        // their FluidState is water; final multipart noCollision checks solids across the full whale.
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int z = centerZ - 2; z <= centerZ + 2; z++) {
                for (int y = bottomY; y <= bottomY + 3; y++) {
                    if (!level.getFluidState(new BlockPos(x, y, z)).is(FluidTags.WATER)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
