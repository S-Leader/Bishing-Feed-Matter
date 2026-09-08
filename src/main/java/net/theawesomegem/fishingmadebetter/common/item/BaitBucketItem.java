package net.theawesomegem.fishingmadebetter.common.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BaitBucketItem extends Item {
    private static final String BAIT_ID = "BaitId";
    private static final String BAIT_METADATA = "BaitMetadata";
    private static final String BAIT_DISPLAY_NAME = "BaitDisplayName";
    private static final String BAIT_COUNT = "BaitCount";

    public BaitBucketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fishingmadebetter.bait_bucket.tooltip").withStyle(ChatFormatting.GRAY));

        String baitId = getBaitId(stack);
        MutableComponent contains = Component.translatable("item.fishingmadebetter.bait_bucket.tooltip.contains").append(": ");
        if (baitId.isEmpty()) {
            tooltip.add(contains
                    .append(Component.translatable("item.fishingmadebetter.bait_bucket.tooltip.none"))
                    .withStyle(ChatFormatting.BLUE));
            return;
        }

        tooltip.add(contains
                .append(Component.literal(getBaitCount(stack) + " " + getBaitDisplayName(stack)))
                .withStyle(ChatFormatting.BLUE));
    }

    public static String getBaitId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(BAIT_ID);
    }

    public static void setBaitId(ItemStack stack, String baitId) {
        stack.getOrCreateTag().putString(BAIT_ID, baitId);
    }

    public static int getBaitMetadata(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(BAIT_METADATA);
    }

    public static void setBaitMetadata(ItemStack stack, int metadata) {
        stack.getOrCreateTag().putInt(BAIT_METADATA, metadata);
    }

    public static String getBaitDisplayName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(BAIT_DISPLAY_NAME);
    }

    public static void setBaitDisplayName(ItemStack stack, String displayName) {
        stack.getOrCreateTag().putString(BAIT_DISPLAY_NAME, displayName);
    }

    public static int getBaitCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(BAIT_COUNT);
    }

    public static void setBaitCount(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt(BAIT_COUNT, count);
    }

    public static void removeBait(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        tag.remove(BAIT_ID);
        tag.remove(BAIT_METADATA);
        tag.remove(BAIT_DISPLAY_NAME);
        tag.remove(BAIT_COUNT);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }
}
