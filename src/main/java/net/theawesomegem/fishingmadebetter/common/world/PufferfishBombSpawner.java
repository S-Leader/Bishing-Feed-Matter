package net.theawesomegem.fishingmadebetter.common.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PufferfishBomb;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class PufferfishBombSpawner {
    private static final String CONVERSION_KEY = "fishingmadebetter:convert_to_pufferfish_bomb";
    private static final Map<ServerLevel, Queue<UUID>> PENDING = Collections.synchronizedMap(new WeakHashMap<>());

    private PufferfishBombSpawner() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity().getType() == EntityType.PUFFERFISH && event.getSpawnType() != MobSpawnType.BUCKET
                && !event.isSpawnCancelled() && event.getEntity().getRandom().nextInt(100) == 0) {
            event.getEntity().getPersistentData().putBoolean(CONVERSION_KEY, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Pufferfish fish
                && fish.getPersistentData().getBoolean(CONVERSION_KEY)) {
            fish.getPersistentData().remove(CONVERSION_KEY);
            PENDING.computeIfAbsent(level, ignored -> new ConcurrentLinkedQueue<>()).add(fish.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
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
                PufferfishBomb bomb = fish.convertTo(ModEntities.PUFFERFISH_BOMB.get(), true);
                if (bomb != null) {
                    bomb.setPuffState(fish.getPuffState());
                    bomb.setFromBucket(fish.fromBucket());
                    bomb.setDeltaMovement(fish.getDeltaMovement());
                    bomb.setAirSupply(fish.getAirSupply());
                    bomb.finalizeSpawn(level, level.getCurrentDifficultyAt(bomb.blockPosition()), MobSpawnType.CONVERSION, null, null);
                }
            }
        }
        if (pending.isEmpty()) {
            PENDING.remove(level);
        }
    }
}
