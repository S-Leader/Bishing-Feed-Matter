package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.LavaFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.VoidFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WaterFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WhaleEntity;
import net.theawesomegem.fishingmadebetter.common.entity.fish.*;

import java.util.List;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<WaterFishingHook>> WATER_FISHING_HOOK = ENTITIES.register("water_fishing_hook", () -> EntityType.Builder.<WaterFishingHook>of(WaterFishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "water_fishing_hook"))));
    public static final DeferredHolder<EntityType<?>, EntityType<LavaFishingHook>> LAVA_FISHING_HOOK = ENTITIES.register("lava_fishing_hook", () -> EntityType.Builder.<LavaFishingHook>of(LavaFishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "lava_fishing_hook"))));
    public static final DeferredHolder<EntityType<?>, EntityType<VoidFishingHook>> VOID_FISHING_HOOK = ENTITIES.register("void_fishing_hook", () -> EntityType.Builder.<VoidFishingHook>of(VoidFishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "void_fishing_hook"))));
    public static final DeferredHolder<EntityType<?>, EntityType<WhaleEntity>> WHALE = ENTITIES.register("whale", () -> EntityType.Builder.of(WhaleEntity::new, MobCategory.WATER_CREATURE).sized(3.60F, 3.75F).eyeHeight(3.75F * .86F).clientTrackingRange(12).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "whale"))));

    public static final DeferredHolder<EntityType<?>, EntityType<ElectricEel>> ELECTRIC_EEL = ENTITIES.register("electric_eel", () -> EntityType.Builder.of(ElectricEel::new, MobCategory.WATER_AMBIENT).sized(1.2F, 0.3F).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "electric_eel"))));
    public static final DeferredHolder<EntityType<?>, EntityType<PortedFish>> REAVER_SHARK = ENTITIES.register("reaver_shark", () -> EntityType.Builder.of(PortedFish::new, MobCategory.WATER_AMBIENT).sized(1.8F, 0.6F).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "reaver_shark"))));
    public static final DeferredHolder<EntityType<?>, EntityType<PortedFish>> PINK_STARFISH = ENTITIES.register("pink_starfish", () -> EntityType.Builder.of(PortedFish::new, MobCategory.WATER_AMBIENT).sized(0.65F, 0.15F).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pink_starfish"))));
    public static final DeferredHolder<EntityType<?>, EntityType<PufferfishBomb>> PUFFERFISH_BOMB = ENTITIES.register("pufferfish_bomb", () -> EntityType.Builder.of(PufferfishBomb::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.7F).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pufferfish_bomb"))));
    public static final DeferredHolder<EntityType<?>, EntityType<PortedFish>> LANTERN_FISH = ENTITIES.register("lantern_fish", () -> EntityType.Builder.of(PortedFish::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.25F).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "lantern_fish"))));
    public static final DeferredHolder<EntityType<?>, EntityType<PinkStarfishProjectile>> STARFISH_PROJECTILE = ENTITIES.register("pink_starfish_projectile", () -> EntityType.Builder.<PinkStarfishProjectile>of(PinkStarfishProjectile::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pink_starfish_projectile"))));
    public static final DeferredHolder<EntityType<?>, EntityType<ElectricEelWhip>> EEL_WHIP = ENTITIES.register("electric_eel_whip", () -> EntityType.Builder.<ElectricEelWhip>of(ElectricEelWhip::new, MobCategory.MISC).sized(0.1F, 0.1F).clientTrackingRange(12).updateInterval(1).noSave().build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "electric_eel_whip"))));

    public static final List<? extends DeferredHolder<EntityType<?>, ? extends EntityType<? extends PortedFish>>> FISH = List.of(ELECTRIC_EEL, REAVER_SHARK, PINK_STARFISH, PUFFERFISH_BOMB, LANTERN_FISH);

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ModEntities::registerAttributes);
        bus.addListener(ModEntities::registerSpawnPlacements);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WHALE.get(), WhaleEntity.createAttributes().build());
        event.put(ELECTRIC_EEL.get(), PortedFish.attributes(25, 1).build());
        event.put(REAVER_SHARK.get(), PortedFish.attributes(25, 1).build());
        event.put(PINK_STARFISH.get(), PortedFish.attributes(10, 0).build());
        event.put(PUFFERFISH_BOMB.get(), PortedFish.attributes(6, 1).build());
        event.put(LANTERN_FISH.get(), PortedFish.attributes(3, 0.8).build());
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (var fish : FISH) {
            event.register(fish.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PortedFish::checkSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }
}
