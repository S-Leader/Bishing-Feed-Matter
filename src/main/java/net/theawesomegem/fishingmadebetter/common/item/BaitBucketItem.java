package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.theawesomegem.fishingmadebetter.common.util.StackData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BaitBucketItem extends Item {
    private static final String BAIT_ID = "BaitId";
    private static final String BAIT_METADATA = "BaitMetadata";
    private static final String BAIT_DISPLAY_NAME = "BaitDisplayName";
    private static final String BAIT_COUNT = "BaitCount";

    public BaitBucketItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> output,
            TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        try {
            tooltip.add(
                    Component.translatable("item.fishingmadebetter.bait_bucket.tooltip")
                            .withStyle(ChatFormatting.GRAY));

            String baitId = getBaitId(stack);
            MutableComponent contains =
                    Component.translatable("item.fishingmadebetter.bait_bucket.tooltip.contains")
                            .append(": ");
            if (baitId.isEmpty()) {
                tooltip.add(
                        contains.append(
                                        Component.translatable(
                                                "item.fishingmadebetter.bait_bucket.tooltip.none"))
                                .withStyle(ChatFormatting.BLUE));
                return;
            }

            tooltip.add(
                    contains.append(
                                    Component.literal(
                                            getBaitCount(stack) + " " + getBaitDisplayName(stack)))
                            .withStyle(ChatFormatting.BLUE));

        } finally {
            tooltip.forEach(output);
        }
    }

    public static String getBaitId(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        return tag == null ? "" : tag.getStringOr(BAIT_ID, "");
    }

    public static void setBaitId(ItemStack stack, String baitId) {
        StackData.update(stack, tag -> tag.putString(BAIT_ID, baitId));
    }

    public static int getBaitMetadata(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        return tag == null ? 0 : tag.getIntOr(BAIT_METADATA, 0);
    }

    public static void setBaitMetadata(ItemStack stack, int metadata) {
        StackData.update(stack, tag -> tag.putInt(BAIT_METADATA, metadata));
    }

    public static String getBaitDisplayName(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        return tag == null ? "" : tag.getStringOr(BAIT_DISPLAY_NAME, "");
    }

    public static void setBaitDisplayName(ItemStack stack, String displayName) {
        StackData.update(stack, tag -> tag.putString(BAIT_DISPLAY_NAME, displayName));
    }

    public static int getBaitCount(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        return tag == null ? 0 : tag.getIntOr(BAIT_COUNT, 0);
    }

    public static void setBaitCount(ItemStack stack, int count) {
        StackData.update(stack, tag -> tag.putInt(BAIT_COUNT, count));
    }

    public static void removeBait(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        if (tag == null) {
            return;
        }

        tag.remove(BAIT_ID);
        tag.remove(BAIT_METADATA);
        tag.remove(BAIT_DISPLAY_NAME);
        tag.remove(BAIT_COUNT);
        StackData.set(stack, tag);
    }
}
