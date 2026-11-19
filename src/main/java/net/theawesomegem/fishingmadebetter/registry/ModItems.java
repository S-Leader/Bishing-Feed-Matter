package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.item.*;
import net.theawesomegem.fishingmadebetter.common.item.KnifeItem.KnifeType;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.item.fish.*;
import net.theawesomegem.fishingmadebetter.common.item.tracker.FishTrackerItem;
import net.theawesomegem.fishingmadebetter.common.item.tracker.FishTrackerItem.TrackingLiquid;
import net.theawesomegem.fishingmadebetter.common.item.tracker.FishTrackerItem.TrackingVision;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredItem<Item> BAIT_BOX = ITEMS.registerItem("baitbox", properties -> new BaitBoxItem(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "baitbox")), properties));
    public static final DeferredItem<Item> FISHING_ROD_WOOD = ITEMS.registerItem("fishing_rod_wood", properties -> new BetterFishingRodItem(ToolMaterial.WOOD, properties));
    public static final DeferredItem<Item> FISHING_ROD_IRON = ITEMS.registerItem("fishing_rod_iron", properties -> new BetterFishingRodItem(ToolMaterial.IRON, properties));
    public static final DeferredItem<Item> FISHING_ROD_DIAMOND = ITEMS.registerItem("fishing_rod_diamond", properties -> new BetterFishingRodItem(ToolMaterial.DIAMOND, properties));
    public static final DeferredItem<Item> FISHING_ROD_STEEL = ITEMS.registerItem("fishing_rod_steel", properties -> new BetterFishingRodItem(SteelTier.INSTANCE, properties));
    public static final DeferredItem<Item> FISHING_ROD_NETHERITE = ITEMS.registerItem("fishing_rod_netherite", properties -> new BetterFishingRodItem(ToolMaterial.NETHERITE, properties.fireResistant()));
    public static final DeferredItem<Item> FILLET_KNIFE_WOOD = ITEMS.registerItem("fillet_knife_wood", properties -> new KnifeItem(ToolMaterial.WOOD, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET, properties));
    public static final DeferredItem<Item> FILLET_KNIFE_IRON = ITEMS.registerItem("fillet_knife_iron", properties -> new KnifeItem(ToolMaterial.IRON, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET, properties));
    public static final DeferredItem<Item> FILLET_KNIFE_DIAMOND = ITEMS.registerItem("fillet_knife_diamond", properties -> new KnifeItem(ToolMaterial.DIAMOND, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET, properties));
    public static final DeferredItem<Item> FILLET_KNIFE_STEEL = ITEMS.registerItem("fillet_knife_steel", properties -> new KnifeItem(SteelTier.INSTANCE, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET, properties));
    public static final DeferredItem<Item> FILLET_KNIFE_NETHERITE = ITEMS.registerItem("fillet_knife_netherite", properties -> new KnifeItem(ToolMaterial.NETHERITE, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET, properties.fireResistant()));
    public static final DeferredItem<Item> SCALING_KNIFE_WOOD = ITEMS.registerItem("scaling_knife_wood", properties -> new KnifeItem(ToolMaterial.WOOD, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING, properties));
    public static final DeferredItem<Item> SCALING_KNIFE_IRON = ITEMS.registerItem("scaling_knife_iron", properties -> new KnifeItem(ToolMaterial.IRON, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING, properties));
    public static final DeferredItem<Item> SCALING_KNIFE_DIAMOND = ITEMS.registerItem("scaling_knife_diamond", properties -> new KnifeItem(ToolMaterial.DIAMOND, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING, properties));
    public static final DeferredItem<Item> SCALING_KNIFE_STEEL = ITEMS.registerItem("scaling_knife_steel", properties -> new KnifeItem(SteelTier.INSTANCE, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING, properties));
    public static final DeferredItem<Item> SCALING_KNIFE_NETHERITE = ITEMS.registerItem("scaling_knife_netherite", properties -> new KnifeItem(ToolMaterial.NETHERITE, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING, properties.fireResistant()));
    public static final DeferredItem<Item> FISH_SLICE_RAW = ITEMS.registerItem("fish_slice_raw", properties -> new FishSliceItem(2, 0.1F, properties));
    public static final DeferredItem<Item> FISH_SLICE_COOKED = ITEMS.registerItem("fish_slice_cooked", properties -> new FishSliceItem(6, 0.8F, properties));
    public static final DeferredItem<Item> FISH_TRACKER_IRON = ITEMS.registerItem("fish_tracker_iron", properties -> new FishTrackerItem(TrackingVision.BAD, TrackingLiquid.WATER, 50, properties));
    public static final DeferredItem<Item> FISH_TRACKER_GOLD = ITEMS.registerItem("fish_tracker_gold", properties -> new FishTrackerItem(TrackingVision.NORMAL, TrackingLiquid.WATER, 100, properties));
    public static final DeferredItem<Item> FISH_TRACKER_DIAMOND = ITEMS.registerItem("fish_tracker_diamond", properties -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.WATER, 150, properties));
    public static final DeferredItem<Item> FISH_TRACKER_OBSIDIAN = ITEMS.registerItem("fish_tracker_obsidian", properties -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.LAVA, 100, properties));
    public static final DeferredItem<Item> FISH_TRACKER_VOID = ITEMS.registerItem("fish_tracker_void", properties -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.VOID, 100, properties));
    public static final DeferredItem<Item> REEL_BASIC = ITEMS.registerItem("reel_basic", properties -> new ReelItem(properties.stacksTo(16), 40, 2, true));
    public static final DeferredItem<Item> REEL_FAST = ITEMS.registerItem("reel_fast", properties -> new ReelItem(properties.durability(192), 75, 6, false));
    public static final DeferredItem<Item> REEL_LONG = ITEMS.registerItem("reel_long", properties -> new ReelItem(properties.durability(256), 150, 4, false));
    public static final DeferredItem<Item> BOBBER_BASIC = ITEMS.registerItem("bobber_basic", properties -> new BobberItem(properties.stacksTo(16), false, false, 0, 0, true));
    public static final DeferredItem<Item> BOBBER_OBSIDIAN = ITEMS.registerItem("bobber_obsidian", properties -> new BobberItem(properties.durability(512), true, false, 0, 0, false));
    public static final DeferredItem<Item> BOBBER_VOID = ITEMS.registerItem("bobber_void", properties -> new BobberItem(properties.durability(128), false, true, 0, 0, false));
    public static final DeferredItem<Item> BOBBER_HEAVY = ITEMS.registerItem("bobber_heavy", properties -> new BobberItem(properties.durability(256), false, false, 3, 0, false));
    public static final DeferredItem<Item> BOBBER_LIGHTWEIGHT = ITEMS.registerItem("bobber_lightweight", properties -> new BobberItem(properties.durability(128), false, false, 0, 4, false));
    public static final DeferredItem<Item> HOOK_BASIC = ITEMS.registerItem("hook_basic", properties -> new HookItem(properties.stacksTo(16), 0, 0, 0, 0, true));
    public static final DeferredItem<Item> HOOK_BARBED = ITEMS.registerItem("hook_barbed", properties -> new HookItem(properties.durability(256), 4, 0, 0, 0, false));
    public static final DeferredItem<Item> HOOK_FATTY = ITEMS.registerItem("hook_fatty", properties -> new HookItem(properties.durability(64), 0, 0, 0, 35, false));
    public static final DeferredItem<Item> HOOK_SHINY = ITEMS.registerItem("hook_shiny", properties -> new HookItem(properties.durability(192), 0, 0, 20, 0, false));
    public static final DeferredItem<Item> HOOK_MAGNETIC = ITEMS.registerItem("hook_magnetic", properties -> new HookItem(properties.durability(256), 0, 20, 0, 0, false));
    public static final DeferredItem<Item> FISH_BUCKET = ITEMS.registerItem("fish_bucket", FishBucketItem::new);
    public static final DeferredItem<Item> BAIT_BUCKET = ITEMS.registerItem("bait_bucket", BaitBucketItem::new);

    public static final DeferredItem<Item> WHALE = ITEMS.registerItem("whale", properties -> new Item(properties));
    public static final DeferredItem<Item> ELECTRIC_EEL_ITEM = ITEMS.registerItem("electric_eel", ElectricEelItem::new);
    public static final DeferredItem<Item> REAVER_SHARK_ITEM = ITEMS.registerItem("reaver_shark", properties -> new Item(properties.pickaxe(ToolMaterial.DIAMOND, 3, -3F).food(FishFoods.BIG_RAW_FISH)));
    public static final DeferredItem<Item> PINK_STARFISH_ITEM = ITEMS.registerItem("pink_starfish", PinkStarfishItem::new);
    public static final DeferredItem<Item> PUFFERFISH_BOMB_ITEM = ITEMS.registerItem("pufferfish_bomb", PufferfishBombItem::new);
    public static final DeferredItem<Item> LANTERN_FISH_ITEM = ITEMS.registerItem("lantern_fish", LanternFishItem::new);

    public static final DeferredItem<Item> WHALE_BUCKET = ITEMS.registerItem("whale_bucket", WhaleBucketItem::new);
    public static final DeferredItem<Item> ELECTRIC_EEL_BUCKET = ITEMS.registerItem("electric_eel_bucket", properties -> new MobBucketItem(ModEntities.ELECTRIC_EEL.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties.stacksTo(1)));
    public static final DeferredItem<Item> REAVER_SHARK_BUCKET = ITEMS.registerItem("reaver_shark_bucket", properties -> new MobBucketItem(ModEntities.REAVER_SHARK.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties.stacksTo(1)));
    public static final DeferredItem<Item> PINK_STARFISH_BUCKET = ITEMS.registerItem("pink_starfish_bucket", properties -> new MobBucketItem(ModEntities.PINK_STARFISH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties.stacksTo(1)));
    public static final DeferredItem<Item> PUFFERFISH_BOMB_BUCKET = ITEMS.registerItem("pufferfish_bomb_bucket", properties -> new MobBucketItem(ModEntities.PUFFERFISH_BOMB.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties.stacksTo(1)));
    public static final DeferredItem<Item> LANTERN_FISH_BUCKET = ITEMS.registerItem("lantern_fish_bucket", properties -> new MobBucketItem(ModEntities.LANTERN_FISH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties.stacksTo(1)));

    public static final DeferredItem<Item> WHALE_STEAK = ITEMS.registerItem("whale_steak", properties -> new Item(properties.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())));
    public static final DeferredItem<Item> WHALE_STEAK_COOKED = ITEMS.registerItem("whale_steak_cooked", properties -> new Item(properties.food(new FoodProperties.Builder().nutrition(10).saturationModifier(0.8F).build())));
    public static final DeferredItem<Item> WHALE_BURGER = ITEMS.registerItem("whale_burger", properties -> new Item(properties.food(new FoodProperties.Builder().nutrition(20).saturationModifier(0.8F).build())));

    public static final DeferredItem<Item> WHALE_SPAWN_EGG = ITEMS.registerItem("whale_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.WHALE.get())));
    public static final DeferredItem<Item> ELECTRIC_EEL_SPAWN_EGG = ITEMS.registerItem("electric_eel_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.ELECTRIC_EEL.get())));
    public static final DeferredItem<Item> REAVER_SHARK_SPAWN_EGG = ITEMS.registerItem("reaver_shark_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.REAVER_SHARK.get())));
    public static final DeferredItem<Item> PINK_STARFISH_SPAWN_EGG = ITEMS.registerItem("pink_starfish_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.PINK_STARFISH.get())));
    public static final DeferredItem<Item> PUFFERFISH_BOMB_SPAWN_EGG = ITEMS.registerItem("pufferfish_bomb_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.PUFFERFISH_BOMB.get())));
    public static final DeferredItem<Item> LANTERN_FISH_SPAWN_EGG = ITEMS.registerItem("lantern_fish_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.LANTERN_FISH.get())));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static Item get(String path) {
        return ITEMS.getEntries().stream().filter(item -> item.getId().getPath().equals(path)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown Kitchen Made Better item: " + path)).get();
    }
}
