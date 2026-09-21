package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class WhaleBucketItem extends Item {
    private static final String IMPOSSIBLE_MESSAGE = "notif.fishingmadebetter.whale_bucket.impossible";

    public WhaleBucketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        showImpossibleMessage(level, player);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        showImpossibleMessage(context.getLevel(), context.getPlayer());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        showImpossibleMessage(player.level(), player);
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private static void showImpossibleMessage(Level level, Player player) {
        if (!level.isClientSide && player != null) {
            player.displayClientMessage(Component.translatable(IMPOSSIBLE_MESSAGE), false);
        }
    }
}
