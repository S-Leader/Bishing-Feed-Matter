package net.theawesomegem.fishingmadebetter.common.util;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.FishBucketItem;

public final class FishStackUtil {
    private static final String FISH_ID = "FishId";
    private static final String FISH_WEIGHT = "FishWeight";
    private static final String FISH_SCALE = "FishScale";
    private static final String FISH_CAUGHT_TIME = "FishCaughtTime";

    private FishStackUtil() {
    }

    public static ItemStack createCaughtFishStack(Item item, FishData fishData, int amount, int weight, long caughtTime) {
        ItemStack stack = new ItemStack(item, amount);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(FISH_ID, fishData.fishId());
        tag.putInt(FISH_WEIGHT, weight);
        // Aquaculture uses this lowercase double tag for tooltips and fish mounts.
        tag.putDouble("fishWeight", weight);
        tag.putLong(FISH_CAUGHT_TIME, caughtTime);
        if (fishData.allowScaling()) {
            tag.putBoolean(FISH_SCALE, true);
        }
        stack.setHoverName(Component.literal(fishData.fishId()).withStyle(ChatFormatting.RESET));
        setLore(stack, weight, fishData.allowScaling(), fishData.allowScaling(), false);
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

    public static int getFishWeight(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FISH_WEIGHT)) {
            return 1;
        }
        return tag.getInt(FISH_WEIGHT);
    }

    public static long getFishCaughtTime(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FISH_CAUGHT_TIME)) {
            return 0L;
        }
        return tag.getLong(FISH_CAUGHT_TIME);
    }

    public static void setFishCaughtTime(ItemStack stack, long time) {
        stack.getOrCreateTag().putLong(FISH_CAUGHT_TIME, time);
    }

    public static boolean hasScale(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(FISH_SCALE) && tag.getBoolean(FISH_SCALE);
    }

    public static void setHasScale(ItemStack stack, boolean value) {
        stack.getOrCreateTag().putBoolean(FISH_SCALE, value);

        // 1.12.2 behavior: scaling a fish immediately kills it, clears its
        // caught time, and rewrites the tooltip to show the detached scale.
        if (!value) {
            setFishCaughtTime(stack, 0L);
            FishData fishData = getFishData(stack);
            setLore(stack, getFishWeight(stack), fishData == null || fishData.allowScaling(), false, true);
        }
    }

    public static boolean isDead(ItemStack stack, long currentTime) {
        FishData fishData = getFishData(stack);
        if (fishData == null) {
            return true;
        }

        long caughtTime = getFishCaughtTime(stack);
        if (caughtTime == 0L) {
            return true;
        }

        if (fishData.allowScaling() && !hasScale(stack)) {
            return true;
        }

        return currentTime >= caughtTime + (fishData.timeAliveOutsideWater() * 20L);
    }

    public static void refreshInventoryState(ItemStack stack, long currentTime) {
        FishData fishData = getFishData(stack);
        if (fishData == null) {
            return;
        }

        boolean dead = isDead(stack, currentTime);
        setLore(stack, getFishWeight(stack), fishData.allowScaling(), hasScale(stack), dead);
        if (dead) {
            setFishCaughtTime(stack, 0L);
        }
    }

    public static boolean isBetterFish(ItemStack stack) {
        String fishId = getFishId(stack);
        return !(stack.getItem() instanceof FishBucketItem) && fishId != null && !fishId.isEmpty();
    }

    @Nullable
    private static FishData getFishData(ItemStack stack) {
        String fishId = getFishId(stack);
        return fishId == null ? null : FishDataRegistry.get(fishId);
    }

    private static void setLore(ItemStack stack, int weight, boolean showScale, boolean hasScale, boolean dead) {
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("Weight: " + weight).withStyle(ChatFormatting.GRAY))));
        if (showScale) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("Scale: " + (hasScale ? "Attached" : "Detached")).withStyle(ChatFormatting.GRAY))));
        }
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(dead ? "Dead" : "Alive").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))));
        stack.getOrCreateTagElement(ItemStack.TAG_DISPLAY).put(ItemStack.TAG_LORE, lore);
    }
}
