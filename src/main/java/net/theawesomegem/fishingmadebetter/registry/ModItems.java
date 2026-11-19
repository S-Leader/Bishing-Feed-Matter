package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final RegistryObject<Item> BAIT_BOX = ITEMS.register("baitbox", () -> new BaitBoxItem(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Constants.MOD_ID, "baitbox")), new Item.Properties()));
    public static final RegistryObject<Item> FISHING_ROD_WOOD = ITEMS.register("fishing_rod_wood", () -> new BetterFishingRodItem(Tiers.WOOD));
    public static final RegistryObject<Item> FISHING_ROD_IRON = ITEMS.register("fishing_rod_iron", () -> new BetterFishingRodItem(Tiers.IRON));
    public static final RegistryObject<Item> FISHING_ROD_DIAMOND = ITEMS.register("fishing_rod_diamond", () -> new BetterFishingRodItem(Tiers.DIAMOND));
    public static final RegistryObject<Item> FISHING_ROD_STEEL = ITEMS.register("fishing_rod_steel", () -> new BetterFishingRodItem(SteelTier.INSTANCE));
    public static final RegistryObject<Item> FISHING_ROD_NETHERITE = ITEMS.register("fishing_rod_netherite", () -> new BetterFishingRodItem(Tiers.NETHERITE, true));
    public static final RegistryObject<Item> FILLET_KNIFE_WOOD = ITEMS.register("fillet_knife_wood", () -> new KnifeItem(Tiers.WOOD, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET));
    public static final RegistryObject<Item> FILLET_KNIFE_IRON = ITEMS.register("fillet_knife_iron", () -> new KnifeItem(Tiers.IRON, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET));
    public static final RegistryObject<Item> FILLET_KNIFE_DIAMOND = ITEMS.register("fillet_knife_diamond", () -> new KnifeItem(Tiers.DIAMOND, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET));
    public static final RegistryObject<Item> FILLET_KNIFE_STEEL = ITEMS.register("fillet_knife_steel", () -> new KnifeItem(SteelTier.INSTANCE, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET));
    public static final RegistryObject<Item> FILLET_KNIFE_NETHERITE = ITEMS.register("fillet_knife_netherite", () -> new KnifeItem(Tiers.NETHERITE, "item.fishingmadebetter.fillet_knife.tooltip", KnifeType.FILLET, true));
    public static final RegistryObject<Item> SCALING_KNIFE_WOOD = ITEMS.register("scaling_knife_wood", () -> new KnifeItem(Tiers.WOOD, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING));
    public static final RegistryObject<Item> SCALING_KNIFE_IRON = ITEMS.register("scaling_knife_iron", () -> new KnifeItem(Tiers.IRON, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING));
    public static final RegistryObject<Item> SCALING_KNIFE_DIAMOND = ITEMS.register("scaling_knife_diamond", () -> new KnifeItem(Tiers.DIAMOND, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING));
    public static final RegistryObject<Item> SCALING_KNIFE_STEEL = ITEMS.register("scaling_knife_steel", () -> new KnifeItem(SteelTier.INSTANCE, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING));
    public static final RegistryObject<Item> SCALING_KNIFE_NETHERITE = ITEMS.register("scaling_knife_netherite", () -> new KnifeItem(Tiers.NETHERITE, "item.fishingmadebetter.scaling_knife.tooltip", KnifeType.SCALING, true));
    public static final RegistryObject<Item> FISH_SLICE_RAW = ITEMS.register("fish_slice_raw", () -> new FishSliceItem(2, 0.1F));
    public static final RegistryObject<Item> FISH_SLICE_COOKED = ITEMS.register("fish_slice_cooked", () -> new FishSliceItem(6, 0.8F));
    public static final RegistryObject<Item> FISH_TRACKER_IRON = ITEMS.register("fish_tracker_iron", () -> new FishTrackerItem(TrackingVision.BAD, TrackingLiquid.WATER, 50));
    public static final RegistryObject<Item> FISH_TRACKER_GOLD = ITEMS.register("fish_tracker_gold", () -> new FishTrackerItem(TrackingVision.NORMAL, TrackingLiquid.WATER, 100));
    public static final RegistryObject<Item> FISH_TRACKER_DIAMOND = ITEMS.register("fish_tracker_diamond", () -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.WATER, 150));
    public static final RegistryObject<Item> FISH_TRACKER_OBSIDIAN = ITEMS.register("fish_tracker_obsidian", () -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.LAVA, 100));
    public static final RegistryObject<Item> FISH_TRACKER_VOID = ITEMS.register("fish_tracker_void", () -> new FishTrackerItem(TrackingVision.BEST, TrackingLiquid.VOID, 100));
    public static final RegistryObject<Item> REEL_BASIC = ITEMS.register("reel_basic", () -> new ReelItem(new Item.Properties().stacksTo(16), 40, 2, true));
    public static final RegistryObject<Item> REEL_FAST = ITEMS.register("reel_fast", () -> new ReelItem(new Item.Properties().durability(192), 75, 6, false));
    public static final RegistryObject<Item> REEL_LONG = ITEMS.register("reel_long", () -> new ReelItem(new Item.Properties().durability(256), 150, 4, false));
    public static final RegistryObject<Item> BOBBER_BASIC = ITEMS.register("bobber_basic", () -> new BobberItem(new Item.Properties().stacksTo(16), false, false, 0, 0, true));
    public static final RegistryObject<Item> BOBBER_OBSIDIAN = ITEMS.register("bobber_obsidian", () -> new BobberItem(new Item.Properties().durability(512), true, false, 0, 0, false));
    public static final RegistryObject<Item> BOBBER_VOID = ITEMS.register("bobber_void", () -> new BobberItem(new Item.Properties().durability(128), false, true, 0, 0, false));
    public static final RegistryObject<Item> BOBBER_HEAVY = ITEMS.register("bobber_heavy", () -> new BobberItem(new Item.Properties().durability(256), false, false, 3, 0, false));
    public static final RegistryObject<Item> BOBBER_LIGHTWEIGHT = ITEMS.register("bobber_lightweight", () -> new BobberItem(new Item.Properties().durability(128), false, false, 0, 4, false));
    public static final RegistryObject<Item> HOOK_BASIC = ITEMS.register("hook_basic", () -> new HookItem(new Item.Properties().stacksTo(16), 0, 0, 0, 0, true));
    public static final RegistryObject<Item> HOOK_BARBED = ITEMS.register("hook_barbed", () -> new HookItem(new Item.Properties().durability(256), 4, 0, 0, 0, false));
    public static final RegistryObject<Item> HOOK_FATTY = ITEMS.register("hook_fatty", () -> new HookItem(new Item.Properties().durability(64), 0, 0, 0, 35, false));
    public static final RegistryObject<Item> HOOK_SHINY = ITEMS.register("hook_shiny", () -> new HookItem(new Item.Properties().durability(192), 0, 0, 20, 0, false));
    public static final RegistryObject<Item> HOOK_MAGNETIC = ITEMS.register("hook_magnetic", () -> new HookItem(new Item.Properties().durability(256), 0, 20, 0, 0, false));
    public static final RegistryObject<Item> FISH_BUCKET = ITEMS.register("fish_bucket", FishBucketItem::new);
    public static final RegistryObject<Item> BAIT_BUCKET = ITEMS.register("bait_bucket", BaitBucketItem::new);

    public static final RegistryObject<Item> WHALE = ITEMS.register("whale", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELECTRIC_EEL_ITEM = ITEMS.register("electric_eel", ElectricEelItem::new);
    public static final RegistryObject<Item> REAVER_SHARK_ITEM = ITEMS.register("reaver_shark", () -> new PickaxeItem(Tiers.DIAMOND, 3, -3F, new Item.Properties().food(FishFoods.BIG_RAW_FISH)));
    public static final RegistryObject<Item> PINK_STARFISH_ITEM = ITEMS.register("pink_starfish", PinkStarfishItem::new);
    public static final RegistryObject<Item> PUFFERFISH_BOMB_ITEM = ITEMS.register("pufferfish_bomb", PufferfishBombItem::new);
    public static final RegistryObject<Item> LANTERN_FISH_ITEM = ITEMS.register("lantern_fish", LanternFishItem::new);

    public static final RegistryObject<Item> WHALE_BUCKET = ITEMS.register("whale_bucket", WhaleBucketItem::new);
    public static final RegistryObject<Item> ELECTRIC_EEL_BUCKET = ITEMS.register("electric_eel_bucket", () -> new MobBucketItem(ModEntities.ELECTRIC_EEL, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REAVER_SHARK_BUCKET = ITEMS.register("reaver_shark_bucket", () -> new MobBucketItem(ModEntities.REAVER_SHARK, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PINK_STARFISH_BUCKET = ITEMS.register("pink_starfish_bucket", () -> new MobBucketItem(ModEntities.PINK_STARFISH, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PUFFERFISH_BOMB_BUCKET = ITEMS.register("pufferfish_bomb_bucket", () -> new MobBucketItem(ModEntities.PUFFERFISH_BOMB, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LANTERN_FISH_BUCKET = ITEMS.register("lantern_fish_bucket", () -> new MobBucketItem(ModEntities.LANTERN_FISH, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WHALE_STEAK = ITEMS.register("whale_steak", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).meat().build())));
    public static final RegistryObject<Item> WHALE_STEAK_COOKED = ITEMS.register("whale_steak_cooked", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(10).saturationMod(0.8F).meat().build())));
    public static final RegistryObject<Item> WHALE_BURGER = ITEMS.register("whale_burger", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(20).saturationMod(0.8F).meat().build())));

    public static final RegistryObject<Item> WHALE_SPAWN_EGG = ITEMS.register("whale_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.WHALE, 0x536C8C, 0xB8C7D9, new Item.Properties()));
    public static final RegistryObject<Item> ELECTRIC_EEL_SPAWN_EGG = ITEMS.register("electric_eel_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.ELECTRIC_EEL, 0X1ccc82, 0x6ff5c7, new Item.Properties()));
    public static final RegistryObject<Item> REAVER_SHARK_SPAWN_EGG = ITEMS.register("reaver_shark_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.REAVER_SHARK, 0X2E2A42, 0XEBECFC, new Item.Properties()));
    public static final RegistryObject<Item> PINK_STARFISH_SPAWN_EGG = ITEMS.register("pink_starfish_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.PINK_STARFISH, 0XF56F79, 0XFCFCFC, new Item.Properties()));
    public static final RegistryObject<Item> PUFFERFISH_BOMB_SPAWN_EGG = ITEMS.register("pufferfish_bomb_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.PUFFERFISH_BOMB, 0X1d2b26, 0xC56A88, new Item.Properties()));
    public static final RegistryObject<Item> LANTERN_FISH_SPAWN_EGG = ITEMS.register("lantern_fish_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.LANTERN_FISH, 0x384858, 0XFCF7B7, new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static Item get(String path) {
        return ITEMS.getEntries().stream().filter(item -> item.getId().getPath().equals(path)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown Kitchen Made Better item: " + path)).get();
    }
}
