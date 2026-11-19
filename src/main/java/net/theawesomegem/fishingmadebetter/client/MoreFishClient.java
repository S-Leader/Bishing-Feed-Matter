package net.theawesomegem.fishingmadebetter.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.*;
import net.theawesomegem.fishingmadebetter.client.particle.*;
import net.theawesomegem.fishingmadebetter.client.renderer.fish.EelWhipRenderer;
import net.theawesomegem.fishingmadebetter.client.renderer.fish.LanternFishHelmetGlowLayer;
import net.theawesomegem.fishingmadebetter.client.renderer.fish.PortedFishRenderer;
import net.theawesomegem.fishingmadebetter.client.renderer.fish.StarfishProjectileRenderer;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;
import net.theawesomegem.fishingmadebetter.registry.ModItems;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class MoreFishClient {
    @SubscribeEvent
    public static void particleGroups(RegisterParticleGroupsEvent event) {
        event.register(ElectricParticleRenderTypes.ZAP, ZapParticleGroup::new);
    }

    @SubscribeEvent
    public static void pipelines(RegisterRenderPipelinesEvent event) {
        ElectricParticleRenderTypes.register(event);
    }

    @SubscribeEvent
    public static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.ZAP.get(), ZapParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPARK.get(), SparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.AIR_BUBBLE.get(), AirBubbleParticle.Provider::new);
    }

    @SubscribeEvent
    public static void armorLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var renderer = event.getPlayerRenderer(skin);
            addHelmetGlow(renderer);
        }
        addHelmetGlow(event.getRenderer(EntityType.ARMOR_STAND));
        addHelmetGlow(event.getRenderer(EntityType.ZOMBIE));
        addHelmetGlow(event.getRenderer(EntityType.ZOMBIE_VILLAGER));
        addHelmetGlow(event.getRenderer(EntityType.HUSK));
        addHelmetGlow(event.getRenderer(EntityType.DROWNED));
        addHelmetGlow(event.getRenderer(EntityType.GIANT));
        addHelmetGlow(event.getRenderer(EntityType.SKELETON));
        addHelmetGlow(event.getRenderer(EntityType.STRAY));
        addHelmetGlow(event.getRenderer(EntityType.WITHER_SKELETON));
        addHelmetGlow(event.getRenderer(EntityType.PIGLIN));
        addHelmetGlow(event.getRenderer(EntityType.PIGLIN_BRUTE));
        addHelmetGlow(event.getRenderer(EntityType.ZOMBIFIED_PIGLIN));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addHelmetGlow(EntityRenderer renderer) {
        if (renderer instanceof LivingEntityRenderer living && living.getModel() instanceof HumanoidModel)
            living.addLayer(new LanternFishHelmetGlowLayer(living));
    }

    @SubscribeEvent
    public static void extensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private LanternFishHelmetModel model;

            @Override
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (model == null) model = new LanternFishHelmetModel();
                return model;
            }
        }, ModItems.LANTERN_FISH_ITEM.get());
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ElectricEelModel.MODEL_LOCATION, ElectricEelModel::createBodyLayer);
        event.registerLayerDefinition(ReaverSharkModel.MODEL_LOCATION, ReaverSharkModel::createBodyLayer);
        event.registerLayerDefinition(PinkStarfishModel.MODEL_LOCATION, PinkStarfishModel::createBodyLayer);
        event.registerLayerDefinition(PufferfishBombSmallModel.MODEL_LOCATION, PufferfishBombSmallModel::createBodyLayer);
        event.registerLayerDefinition(PufferfishBombMidModel.MODEL_LOCATION, PufferfishBombMidModel::createBodyLayer);
        event.registerLayerDefinition(PufferfishBombBigModel.MODEL_LOCATION, PufferfishBombBigModel::createBodyLayer);
        event.registerLayerDefinition(LanternFishModel.MODEL_LOCATION, LanternFishModel::createBodyLayer);
        event.registerLayerDefinition(ElectricEelItemModel.LAYER_LOCATION, ElectricEelItemModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ELECTRIC_EEL.get(), context -> new PortedFishRenderer(context, "electric_eel", new ElectricEelModel(context), true));
        event.registerEntityRenderer(ModEntities.REAVER_SHARK.get(), context -> new PortedFishRenderer(context, "reaver_shark", new ReaverSharkModel(context), false));
        event.registerEntityRenderer(ModEntities.PINK_STARFISH.get(), context -> new PortedFishRenderer(context, "pink_starfish", new PinkStarfishModel(context), false));
        event.registerEntityRenderer(ModEntities.PUFFERFISH_BOMB.get(), context -> new PortedFishRenderer(context, "pufferfish_bomb", new PufferfishBombBigModel(context), false));
        event.registerEntityRenderer(ModEntities.LANTERN_FISH.get(), context -> new PortedFishRenderer(context, "lantern_fish", new LanternFishModel(context), true));
        event.registerEntityRenderer(ModEntities.STARFISH_PROJECTILE.get(), StarfishProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.EEL_WHIP.get(), EelWhipRenderer::new);
    }
}
