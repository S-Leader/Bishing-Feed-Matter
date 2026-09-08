package net.theawesomegem.fishingmadebetter.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

public class WaterFishingHook extends FmbFishingHook {
    public WaterFishingHook(EntityType<? extends FishingHook> entityType, Level level) {
        super(entityType, level, HookLiquid.WATER);
    }

    public WaterFishingHook(Player player, Level level, ItemStack rodStack) {
        super(ModEntities.WATER_FISHING_HOOK.get(), player, level, HookLiquid.WATER, rodStack);
    }
}
