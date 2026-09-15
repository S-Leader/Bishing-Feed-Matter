package net.theawesomegem.fishingmadebetter.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.client.model.BlueWhaleModel;
import net.theawesomegem.fishingmadebetter.client.renderer.layer.BlueWhaleStuckProjectileLayer;
import net.theawesomegem.fishingmadebetter.common.entity.BlueWhaleEntity;

public final class BlueWhaleRenderer extends MobRenderer<BlueWhaleEntity, BlueWhaleModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/blue_whale.png");

    public BlueWhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new BlueWhaleModel(context.bakeLayer(BlueWhaleModel.LAYER_LOCATION)), 2.2F);
        addLayer(new BlueWhaleStuckProjectileLayer(context, this));
    }

    @Override
    public ResourceLocation getTextureLocation(BlueWhaleEntity whale) {
        return TEXTURE;
    }
}
