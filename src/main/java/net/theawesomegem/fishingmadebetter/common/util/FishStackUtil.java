package net.theawesomegem.fishingmadebetter.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.FishBucketItem;
import net.theawesomegem.fishingmadebetter.compat.AquacultureCompat;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FishStackUtil {
    private static final String FISH_ID = "FishId";
    private static final String FISH_WEIGHT = "FishWeight";
    private static final String FISH_SCALE = "FishScale";
    private static final String FISH_CAUGHT_TIME = "FishCaughtTime";

    public static ItemStack createCaughtFishStack(Item item, FishData fishData, int amount, int weight, long caughtTime) {
        ItemStack stack = new ItemStack(item, amount);
        CompoundTag tag = StackData.copy(stack);
        tag.putString(FISH_ID, fishData.fishId());
        tag.putInt(FISH_WEIGHT, weight);
        // Aquaculture uses this lowercase double tag for tooltips and fish mounts.
        tag.putDouble("fishWeight", weight);
        tag.putLong(FISH_CAUGHT_TIME, caughtTime);
        if (fishData.allowScaling()) {
            tag.putBoolean(FISH_SCALE, true);
        }
        StackData.set(stack, tag);
        AquacultureCompat.setFishWeight(stack, weight);
        stack.set(DataComponents.CUSTOM_NAME, fishData.displayName().copy().withStyle(ChatFormatting.RESET));
        setLore(stack, weight, fishData.allowScaling(), fishData.allowScaling(), false);
        return stack;
    }

    @Nullable
    public static String getFishId(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        if (tag == null || !tag.contains(FISH_ID)) {
            return null;
        }
        return tag.getStringOr(FISH_ID, "");
    }

    public static int getFishWeight(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        if (tag == null || !tag.contains(FISH_WEIGHT)) {
            return 1;
        }
        return tag.getIntOr(FISH_WEIGHT, 0);
    }

    public static long getFishCaughtTime(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        if (tag == null || !tag.contains(FISH_CAUGHT_TIME)) {
            return 0L;
        }
        return tag.getLongOr(FISH_CAUGHT_TIME, 0L);
    }

    public static void setFishCaughtTime(ItemStack stack, long time) {
        StackData.update(stack, tag -> tag.putLong(FISH_CAUGHT_TIME, time));
    }

    public static void attachFishData(ItemStack stack, FishData fishData) {
        CompoundTag tag = StackData.copy(stack);
        tag.putString(FISH_ID, fishData.fishId());
        if (!tag.contains(FISH_WEIGHT)) {
            tag.putInt(FISH_WEIGHT, 1);
        }
        if (!tag.contains("fishWeight")) {
            tag.putDouble("fishWeight", getFishWeight(stack));
        }
        StackData.set(stack, tag);
        AquacultureCompat.setFishWeight(stack, getFishWeight(stack));
    }

    public static boolean hasScale(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        return tag != null && tag.contains(FISH_SCALE) && tag.getBooleanOr(FISH_SCALE, false);
    }

    public static void setHasScale(ItemStack stack, boolean value) {
        StackData.update(stack, tag -> tag.putBoolean(FISH_SCALE, value));

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
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Weight: " + weight).withStyle(ChatFormatting.GRAY));
        if (showScale) {
            lines.add(Component.literal("Scale: " + (hasScale ? "Attached" : "Detached")).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.literal(dead ? "Dead" : "Alive").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
        ItemLore lore = new ItemLore(lines);
        if (!lore.equals(stack.get(DataComponents.LORE))) stack.set(DataComponents.LORE, lore);
    }
}
