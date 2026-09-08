package net.theawesomegem.fishingmadebetter.common.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishData.FishingLiquid;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.data.FishPopulationSavedData;

public class FishBucketItem extends Item {
    private static final String FISH_ID = "FishId";

    public FishBucketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        String fishId = getFishId(stack);
        if (fishId == null) {
            return InteractionResultHolder.fail(stack);
        }
        FishData fishData = FishDataRegistry.get(fishId);
        if (fishData == null) {
            return InteractionResultHolder.fail(stack);
        }

        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(Fluids.WATER)) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_bucket.only_water"), false);
            return InteractionResultHolder.fail(stack);
        }

        if (countNearbyWater(level, pos) < 25) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_bucket.small_water"), false);
            return InteractionResultHolder.fail(stack);
        }

        if (fishData.liquid() != FishingLiquid.WATER && fishData.liquid() != FishingLiquid.ANY) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_bucket.only_water"), false);
            return InteractionResultHolder.fail(stack);
        }
        if (fishData.minYLevel() > pos.getY() || fishData.maxYLevel() < pos.getY()) {
            player.displayClientMessage(Component.translatable("notif.fishingmadebetter.fish_bucket.wrong_altitude"), false);
            return InteractionResultHolder.fail(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            FishPopulationSavedData.get(serverLevel).increasePopulation(level.getChunk(pos).getPos(), fishId, 1, level.getGameTime());
        }

        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private static int countNearbyWater(Level level, BlockPos origin) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -3, -2), origin.offset(2, 0, 2))) {
            if (level.getFluidState(pos).is(Fluids.WATER)) {
                count++;
                if (count >= 25) {
                    return count;
                }
            }
        }
        return count;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String fishId = getFishId(stack);
        if (fishId != null) {
            tooltip.add(Component.translatable("item.fishingmadebetter.fish_bucket.tooltip")
                    .append(": ")
                    .append(Component.literal(fishId))
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    public ItemStack createStack(String fishId) {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putString(FISH_ID, fishId);
        return stack;
    }

    @Nullable
    public static String getFishId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FISH_ID)) {
            return null;
        }
        return tag.getString(FISH_ID);
    }
}
