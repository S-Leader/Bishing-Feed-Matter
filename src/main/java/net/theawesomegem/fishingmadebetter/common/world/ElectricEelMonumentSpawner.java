package net.theawesomegem.fishingmadebetter.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.config.FmbCommonConfig;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectricEel;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class ElectricEelMonumentSpawner {
    private static final Map<ServerLevel, PendingSpawns> PENDING = Collections.synchronizedMap(new WeakHashMap<>());

    private ElectricEelMonumentSpawner() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Structure monument = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.OCEAN_MONUMENT.location());
        if (monument == null) {
            return;
        }
        StructureStart start = event.getChunk().getStartForStructure(monument);
        if (start != null && start.isValid()) {
            PENDING.computeIfAbsent(level, ignored -> new PendingSpawns()).add(start.getChunkPos().toLong());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)
                || level.getGameTime() % 20 != 0 || FmbCommonConfig.electricEelMonumentSpawnChancePercent() == 0) {
            return;
        }
        PendingSpawns pending = PENDING.get(level);
        if (pending == null) {
            return;
        }
        int count = Math.min(4, pending.chunks.size());
        for (int i = 0; i < count; i++) {
            Long chunk = pending.chunks.poll();
            if (chunk == null) {
                break;
            }
            pending.queued.remove(chunk);
            if (inspectMonument(level, new ChunkPos(chunk))) {
                pending.add(chunk);
            }
        }
        if (pending.chunks.isEmpty()) {
            PENDING.remove(level);
        }
    }

    private static boolean inspectMonument(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) {
            return false;
        }
        Structure monument = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.OCEAN_MONUMENT.location());
        if (monument == null) {
            return false;
        }
        StructureStart start = chunk.getStartForStructure(monument);
        if (start == null || !start.isValid()) {
            return false;
        }
        long startChunk = start.getChunkPos().toLong();
        ElectricEelMonumentSavedData data = ElectricEelMonumentSavedData.get(level);
        if (!data.hasChecked(startChunk)) {
            RandomSource random = RandomSource.create(level.getSeed() ^ startChunk * 0x9E3779B97F4A7C15L ^ 0x45454C4D4F4E554DL);
            data.recordRoll(startChunk, random.nextDouble() < FmbCommonConfig.electricEelMonumentSpawnChancePercent() / 100.0);
        }
        if (!data.isPending(startChunk)) {
            return false;
        }
        if (spawnNearby(level, start.getBoundingBox())) {
            data.markSpawned(startChunk);
            return false;
        }
        return true;
    }

    private static boolean spawnNearby(ServerLevel level, BoundingBox monument) {
        ElectricEel eel = ModEntities.ELECTRIC_EEL.get().create(level);
        if (eel == null) {
            return false;
        }
        int minY = Math.max(level.getMinBuildHeight() + 1, monument.minY());
        int maxY = Math.min(level.getSeaLevel() - 2, monument.maxY());
        if (minY > maxY) {
            return false;
        }
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 128; attempt++) {
            int x = Mth.nextInt(random, monument.minX() - 12, monument.maxX() + 12);
            int z = Mth.nextInt(random, monument.minZ() - 12, monument.maxZ() + 12);
            int distance = Mth.nextInt(random, 4, 12);
            switch (random.nextInt(4)) {
                case 0 -> x = monument.minX() - distance;
                case 1 -> x = monument.maxX() + distance;
                case 2 -> z = monument.minZ() - distance;
                case 3 -> z = monument.maxZ() + distance;
            }
            eel.moveTo(x + 0.5, Mth.nextInt(random, minY, maxY), z + 0.5, random.nextFloat() * 360, 0);
            if (!hasWaterSpace(level, eel.getBoundingBox()) || !level.noCollision(eel)) {
                continue;
            }
            eel.finalizeSpawn(level, level.getCurrentDifficultyAt(eel.blockPosition()), MobSpawnType.STRUCTURE, null, null);
            eel.setAirSupply(eel.getMaxAirSupply());
            eel.setPersistenceRequired();
            return level.addFreshEntity(eel);
        }
        return false;
    }

    private static boolean hasWaterSpace(ServerLevel level, AABB bounds) {
        if (!level.getWorldBorder().isWithinBounds(bounds)) {
            return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(bounds.minX), Mth.floor(bounds.minY), Mth.floor(bounds.minZ),
                Mth.floor(bounds.maxX - 1.0E-7), Mth.floor(bounds.maxY - 1.0E-7), Mth.floor(bounds.maxZ - 1.0E-7))) {
            if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null
                    || !level.getFluidState(pos).is(FluidTags.WATER)) {
                return false;
            }
        }
        return true;
    }

    private static final class PendingSpawns {
        private final Queue<Long> chunks = new ConcurrentLinkedQueue<>();
        private final Set<Long> queued = ConcurrentHashMap.newKeySet();

        private void add(long chunk) {
            if (queued.add(chunk)) {
                chunks.add(chunk);
            }
        }
    }
}
