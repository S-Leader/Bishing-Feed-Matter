package net.theawesomegem.fishingmadebetter.common.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PufferfishBomb;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class PufferfishBombSpawner {
    private static final String CONVERSION_KEY = "fishingmadebetter:convert_to_pufferfish_bomb";
    private static final Map<ServerLevel, Queue<UUID>> PENDING = Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity().getType() == EntityType.PUFFERFISH && event.getSpawnType() != EntitySpawnReason.BUCKET && !event.isSpawnCancelled() && event.getEntity().getRandom().nextInt(100) == 0) {
            event.getEntity().getPersistentData().putBoolean(CONVERSION_KEY, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Pufferfish fish && fish.getPersistentData().getBooleanOr(CONVERSION_KEY, false)) {
            fish.getPersistentData().remove(CONVERSION_KEY);
            PENDING.computeIfAbsent(level, ignored -> new ConcurrentLinkedQueue<>()).add(fish.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Queue<UUID> pending = PENDING.get(level);
        if (pending == null) {
            return;
        }
        for (int i = 0; i < 64; i++) {
            UUID id = pending.poll();
            if (id == null) {
                break;
            }
            if (level.getEntity(id) instanceof Pufferfish fish && fish.isAlive()) {
                PufferfishBomb bomb = fish.convertTo(ModEntities.PUFFERFISH_BOMB.get(), ConversionParams.single(fish, true, true), EntitySpawnReason.CONVERSION, converted -> {
                });
                if (bomb != null) {
                    bomb.setPuffState(fish.getPuffState());
                    bomb.setFromBucket(fish.fromBucket());
                    bomb.setDeltaMovement(fish.getDeltaMovement());
                    bomb.setAirSupply(fish.getAirSupply());
                    bomb.finalizeSpawn(level, level.getCurrentDifficultyAt(bomb.blockPosition()), EntitySpawnReason.CONVERSION, null);
                }
            }
        }
        if (pending.isEmpty()) {
            PENDING.remove(level);
        }
    }
}
