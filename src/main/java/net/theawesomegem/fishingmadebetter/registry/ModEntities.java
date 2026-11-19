package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.LavaFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.VoidFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WaterFishingHook;
import net.theawesomegem.fishingmadebetter.common.entity.WhaleEntity;
import net.theawesomegem.fishingmadebetter.common.entity.fish.*;

import java.util.List;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);

    public static final RegistryObject<EntityType<WaterFishingHook>> WATER_FISHING_HOOK = ENTITIES.register("water_fishing_hook", () -> EntityType.Builder.<WaterFishingHook>of(WaterFishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build("water_fishing_hook"));
    public static final RegistryObject<EntityType<LavaFishingHook>> LAVA_FISHING_HOOK = ENTITIES.register("lava_fishing_hook", () -> EntityType.Builder.<LavaFishingHook>of(LavaFishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build("lava_fishing_hook"));
    public static final RegistryObject<EntityType<VoidFishingHook>> VOID_FISHING_HOOK = ENTITIES.register("void_fishing_hook", () -> EntityType.Builder.<VoidFishingHook>of(VoidFishingHook::new, MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5).build("void_fishing_hook"));
    public static final RegistryObject<EntityType<WhaleEntity>> WHALE = ENTITIES.register("whale", () -> EntityType.Builder.of(WhaleEntity::new, MobCategory.WATER_CREATURE).sized(3.60F, 3.75F).clientTrackingRange(12).updateInterval(2).build("whale"));

    public static final RegistryObject<EntityType<ElectricEel>> ELECTRIC_EEL = ENTITIES.register("electric_eel", () -> EntityType.Builder.of(ElectricEel::new, MobCategory.WATER_AMBIENT).sized(1.2F, 0.3F).clientTrackingRange(8).build("electric_eel"));
    public static final RegistryObject<EntityType<PortedFish>> REAVER_SHARK = ENTITIES.register("reaver_shark", () -> EntityType.Builder.of(PortedFish::new, MobCategory.WATER_AMBIENT).sized(1.8F, 0.6F).clientTrackingRange(8).build("reaver_shark"));
    public static final RegistryObject<EntityType<PortedFish>> PINK_STARFISH = ENTITIES.register("pink_starfish", () -> EntityType.Builder.of(PortedFish::new, MobCategory.WATER_AMBIENT).sized(0.65F, 0.15F).clientTrackingRange(8).build("pink_starfish"));
    public static final RegistryObject<EntityType<PufferfishBomb>> PUFFERFISH_BOMB = ENTITIES.register("pufferfish_bomb", () -> EntityType.Builder.of(PufferfishBomb::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.7F).clientTrackingRange(8).updateInterval(1).build("pufferfish_bomb"));
    public static final RegistryObject<EntityType<PortedFish>> LANTERN_FISH = ENTITIES.register("lantern_fish", () -> EntityType.Builder.of(PortedFish::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.25F).clientTrackingRange(8).build("lantern_fish"));
    public static final RegistryObject<EntityType<PinkStarfishProjectile>> STARFISH_PROJECTILE = ENTITIES.register("pink_starfish_projectile", () -> EntityType.Builder.<PinkStarfishProjectile>of(PinkStarfishProjectile::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(8).updateInterval(1).build("pink_starfish_projectile"));
    public static final RegistryObject<EntityType<ElectricEelWhip>> EEL_WHIP = ENTITIES.register("electric_eel_whip", () -> EntityType.Builder.<ElectricEelWhip>of(ElectricEelWhip::new, MobCategory.MISC).sized(0.1F, 0.1F).clientTrackingRange(12).updateInterval(1).noSave().build("electric_eel_whip"));

    public static final List<RegistryObject<? extends EntityType<? extends PortedFish>>> FISH = List.of(ELECTRIC_EEL, REAVER_SHARK, PINK_STARFISH, PUFFERFISH_BOMB, LANTERN_FISH);

    private ModEntities() {
    }

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

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        for (var fish : FISH) {
            event.register(fish.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PortedFish::checkSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }
}
