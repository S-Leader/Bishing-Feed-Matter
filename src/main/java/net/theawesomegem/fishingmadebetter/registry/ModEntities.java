package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WhaleEntity;

import java.util.function.Supplier;

public final class ModEntities {
    public static Supplier<EntityType<? extends FishingHook>> WATER_FISHING_HOOK;
    public static Supplier<EntityType<? extends FishingHook>> LAVA_FISHING_HOOK;
    public static Supplier<EntityType<? extends FishingHook>> VOID_FISHING_HOOK;
    public static Supplier<EntityType<WhaleEntity>> WHALE;

    private ModEntities() {
    }
}
