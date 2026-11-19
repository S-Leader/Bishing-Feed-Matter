package net.theawesomegem.fishingmadebetter.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.fish.*;
import net.theawesomegem.fishingmadebetter.client.particle.AirBubbleParticle;
import net.theawesomegem.fishingmadebetter.client.particle.SparkParticle;
import net.theawesomegem.fishingmadebetter.client.particle.ZapParticle;
import net.theawesomegem.fishingmadebetter.client.renderer.fish.EelWhipRenderer;
import net.theawesomegem.fishingmadebetter.client.renderer.fish.PortedFishRenderer;
import net.theawesomegem.fishingmadebetter.client.renderer.fish.StarfishProjectileRenderer;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MoreFishClient {
    private MoreFishClient() {
    }

    @SubscribeEvent
    public static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.ZAP.get(), ZapParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPARK.get(), SparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.AIR_BUBBLE.get(), AirBubbleParticle.Provider::new);
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
