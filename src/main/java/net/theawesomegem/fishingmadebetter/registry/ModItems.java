package net.theawesomegem.fishingmadebetter.registry;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Tiers;
import net.theawesomegem.fishingmadebetter.common.item.BaitBucketItem;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.FishBucketItem;
import net.theawesomegem.fishingmadebetter.common.item.FishSliceItem;
import net.theawesomegem.fishingmadebetter.common.item.KnifeItem;
import net.theawesomegem.fishingmadebetter.common.item.KnifeItem.KnifeType;
import net.theawesomegem.fishingmadebetter.common.item.SteelTier;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.item.tracker.FishTrackerItem;
import net.theawesomegem.fishingmadebetter.common.item.tracker.FishTrackerItem.TrackingLiquid;
import net.theawesomegem.fishingmadebetter.common.item.tracker.FishTrackerItem.TrackingVision;
import net.theawesomegem.fishingmadebetter.Constants;

public final class ModItems {
    public static final List<ItemDefinition> ITEMS = List.of(
            item("fishing_rod_wood"),
            item("fishing_rod_iron"),
            item("fishing_rod_diamond"),
            item("fishing_rod_steel"),
            item("fishing_rod_netherite"),
            item("fillet_knife_wood"),
            item("fillet_knife_iron"),
            item("fillet_knife_diamond"),
            item("fillet_knife_steel"),
            item("fillet_knife_netherite"),
            item("scaling_knife_wood"),
            item("scaling_knife_iron"),
            item("scaling_knife_diamond"),
            item("scaling_knife_steel"),
            item("scaling_knife_netherite"),
            item("fish_slice_raw"),
            item("fish_slice_cooked"),
            item("fish_tracker_iron"),
            item("fish_tracker_gold"),
            item("fish_tracker_diamond"),
            item("fish_tracker_obsidian"),
            item("fish_tracker_void"),
            item("reel_basic"),
            item("reel_fast"),
            item("reel_long"),
            item("bobber_basic"),
            item("bobber_obsidian"),
            item("bobber_void"),
            item("bobber_heavy"),
            item("bobber_lightweight"),
            item("hook_basic"),
            item("hook_barbed"),
            item("hook_fatty"),
            item("hook_shiny"),
            item("hook_magnetic"),
            item("fish_bucket"),
            item("bait_bucket"),
            item("whale"),
            item("whale_steak"),
            item("whale_burger")
    );

    private ModItems() {
    }

    private static ItemDefinition item(String path) {
        return new ItemDefinition(Constants.MOD_ID, path);
    }

    public static Item create(String path) {
        return switch (path) {
            case "fishing_rod_wood" -> new BetterFishingRodItem(Tiers.WOOD);
            case "fishing_rod_iron" -> new BetterFishingRodItem(Tiers.IRON);
            case "fishing_rod_diamond" -> new BetterFishingRodItem(Tiers.DIAMOND);
            case "fishing_rod_steel" -> new BetterFishingRodItem(SteelTier.INSTANCE);
            case "fishing_rod_netherite" -> new BetterFishingRodItem(Tiers.NETHERITE, true);
            case "fillet_knife_wood" -> new KnifeItem(Tiers.WOOD, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET);
            case "fillet_knife_iron" -> new KnifeItem(Tiers.IRON, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET);
            case "fillet_knife_diamond" -> new KnifeItem(Tiers.DIAMOND, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET);
            case "fillet_knife_steel" -> new KnifeItem(SteelTier.INSTANCE, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET);
            case "fillet_knife_netherite" -> new KnifeItem(Tiers.NETHERITE, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET, true);
            case "scaling_knife_wood" -> new KnifeItem(Tiers.WOOD, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING);
            case "scaling_knife_iron" -> new KnifeItem(Tiers.IRON, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING);
            case "scaling_knife_diamond" -> new KnifeItem(Tiers.DIAMOND, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING);
            case "scaling_knife_steel" -> new KnifeItem(SteelTier.INSTANCE, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING);
            case "scaling_knife_netherite" -> new KnifeItem(Tiers.NETHERITE, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING, true);
            case "fish_slice_raw" -> new FishSliceItem(2, 0.1F);
            case "fish_slice_cooked" -> new FishSliceItem(6, 0.8F);
            case "fish_tracker_iron" -> new FishTrackerItem(TrackingVision.BAD, TrackingLiquid.WATER, 50);
            case "fish_tracker_gold" -> new FishTrackerItem(TrackingVision.NORMAL, TrackingLiquid.WATER, 100);
            case "fish_tracker_diamond" -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.WATER, 150);
            case "fish_tracker_obsidian" -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.LAVA, 100);
            case "fish_tracker_void" -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.VOID, 100);
            case "reel_basic" -> new ReelItem(new Item.Properties().stacksTo(16), 40, 2, true);
            case "reel_fast" -> new ReelItem(new Item.Properties().durability(192), 75, 6, false);
            case "reel_long" -> new ReelItem(new Item.Properties().durability(256), 150, 4, false);
            case "bobber_basic" -> new BobberItem(new Item.Properties().stacksTo(16), false, false, 0, 0, true);
            case "bobber_obsidian" -> new BobberItem(new Item.Properties().durability(512), true, false, 0, 0, false);
            case "bobber_void" -> new BobberItem(new Item.Properties().durability(128), false, true, 0, 0, false);
            case "bobber_heavy" -> new BobberItem(new Item.Properties().durability(256), false, false, 3, 0, false);
            case "bobber_lightweight" -> new BobberItem(new Item.Properties().durability(128), false, false, 0, 4, false);
            case "hook_basic" -> new HookItem(new Item.Properties().stacksTo(16), 0, 0, 0, 0, true);
            case "hook_barbed" -> new HookItem(new Item.Properties().durability(256), 4, 0, 0, 0, false);
            case "hook_fatty" -> new HookItem(new Item.Properties().durability(64), 0, 0, 0, 35, false);
            case "hook_shiny" -> new HookItem(new Item.Properties().durability(192), 0, 0, 20, 0, false);
            case "hook_magnetic" -> new HookItem(new Item.Properties().durability(256), 0, 20, 0, 0, false);
            case "fish_bucket" -> new FishBucketItem();
            case "bait_bucket" -> new BaitBucketItem();
            case "whale" -> new Item(new Item.Properties());
            case "whale_steak" -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).meat().build()));
            case "whale_burger" -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(20).saturationMod(0.8F).meat().build()));
            default -> new Item(new Item.Properties());
        };
    }

    public record ItemDefinition(String namespace, String path) {
    }
}
