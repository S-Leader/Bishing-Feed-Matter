package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.util.PlayerMessages;

public final class WhaleBucketItem extends Item {
    private static final String IMPOSSIBLE_MESSAGE =
            "notif.fishingmadebetter.whale_bucket.impossible";

    public WhaleBucketItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        showImpossibleMessage(level, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        showImpossibleMessage(context.getLevel(), context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        showImpossibleMessage(player.level(), player);
        return InteractionResult.SUCCESS;
    }

    private static void showImpossibleMessage(Level level, Player player) {
        if (!level.isClientSide() && player != null) {
            PlayerMessages.send(player, Component.translatable(IMPOSSIBLE_MESSAGE), false);
        }
    }
}
