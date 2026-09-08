package net.theawesomegem.fishingmadebetter.client;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.FishingRodItem;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;

public final class RodModelProperties {
    public static final ResourceLocation REEL = new ResourceLocation(Constants.MOD_ID, "reel");
    public static final ResourceLocation BOBBER = new ResourceLocation(Constants.MOD_ID, "bobber");
    public static final ResourceLocation HOOK = new ResourceLocation(Constants.MOD_ID, "hook");
    public static final ResourceLocation CAST = new ResourceLocation(Constants.MOD_ID, "cast");
    public static final List<ResourceLocation> ROD_IDS = List.of(
            new ResourceLocation(Constants.MOD_ID, "fishing_rod_wood"),
            new ResourceLocation(Constants.MOD_ID, "fishing_rod_iron"),
            new ResourceLocation(Constants.MOD_ID, "fishing_rod_diamond"),
            new ResourceLocation(Constants.MOD_ID, "fishing_rod_steel"),
            new ResourceLocation(Constants.MOD_ID, "fishing_rod_netherite")
    );

    private RodModelProperties() {
    }

    public static float reel(ItemStack stack) {
        return valueFor(BetterFishingRodItem.getReelItem(stack));
    }

    public static float bobber(ItemStack stack) {
        return valueFor(BetterFishingRodItem.getBobberItem(stack));
    }

    public static float hook(ItemStack stack) {
        return valueFor(BetterFishingRodItem.getHookItem(stack));
    }

    public static float cast(ItemStack stack, LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }

        boolean mainHandRod = entity.getMainHandItem() == stack;
        boolean offHandRod = entity.getOffhandItem() == stack;
        if (entity.getMainHandItem().getItem() instanceof FishingRodItem) {
            offHandRod = false;
        }

        return (mainHandRod || offHandRod) && entity instanceof Player player && player.fishing != null ? 1.0F : 0.0F;
    }

    public static List<Item> rodItems() {
        return ROD_IDS.stream().map(BuiltInRegistries.ITEM::get).toList();
    }

    private static float valueFor(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null || !Constants.MOD_ID.equals(id.getNamespace())) {
            return 0.0F;
        }

        return switch (id.getPath()) {
            case "reel_basic", "bobber_basic", "hook_basic" -> 0.1F;
            case "reel_fast", "bobber_obsidian", "hook_barbed" -> 0.2F;
            case "reel_long", "bobber_void", "hook_fatty" -> 0.3F;
            case "bobber_heavy", "hook_shiny" -> 0.4F;
            case "bobber_lightweight", "hook_magnetic" -> 0.5F;
            default -> 0.0F;
        };
    }
}
