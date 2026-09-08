package net.theawesomegem.fishingmadebetter.registry;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;

public final class ModEntities {
    public static Supplier<EntityType<? extends FishingHook>> WATER_FISHING_HOOK;
    public static Supplier<EntityType<? extends FishingHook>> LAVA_FISHING_HOOK;
    public static Supplier<EntityType<? extends FishingHook>> VOID_FISHING_HOOK;

    private ModEntities() {
    }
}
