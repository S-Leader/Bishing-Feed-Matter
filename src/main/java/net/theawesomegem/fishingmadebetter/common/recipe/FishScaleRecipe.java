package net.theawesomegem.fishingmadebetter.common.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.KnifeItem;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FishScaleRecipe extends CustomRecipe {

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return findSlots(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        Slots slots = findSlots(container);
        if (slots == null) {
            return ItemStack.EMPTY;
        }

        FishData fishData = getFishData(container.getItem(slots.fish()));
        if (fishData == null) {
            return ItemStack.EMPTY;
        }
        Item item = FishDataRegistry.resolveItem(fishData.scalingItem());
        if (item == null) {
            return ItemStack.EMPTY;
        }

        int amount = fishData.scalingUseWeight() ? Math.min(getScaleAmount(FishStackUtil.getFishWeight(container.getItem(slots.fish()))), 64) : 1;
        return new ItemStack(item, amount);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        Slots slots = findSlots(container);
        if (slots == null) {
            return NonNullList.withSize(container.size(), ItemStack.EMPTY);
        }

        ItemStack fish = container.getItem(slots.fish());
        FishData fishData = getFishData(fish);
        int totalScales = fishData != null && fishData.scalingUseWeight() ? getScaleAmount(FishStackUtil.getFishWeight(fish)) : 1;
        int remainingScales = Math.max(0, totalScales - 64);

        NonNullList<ItemStack> remaining = NonNullList.withSize(container.size(), ItemStack.EMPTY);
        ItemStack knife = container.getItem(slots.knife()).copy();
        knife.setDamageValue(knife.getDamageValue() + totalScales);
        if (knife.getDamageValue() < knife.getMaxDamage()) {
            remaining.set(slots.knife(), knife);
        }

        ItemStack scaledFish = fish.copy();
        scaledFish.setCount(1);
        if (fishData != null && !FishStackUtil.isBetterFish(scaledFish)) {
            FishStackUtil.attachFishData(scaledFish, fishData);
        }
        FishStackUtil.setHasScale(scaledFish, false);
        remaining.set(slots.fish(), scaledFish);

        Item scaleItem = fishData == null ? null : FishDataRegistry.resolveItem(fishData.scalingItem());
        for (int i = 0; i < remaining.size() && remainingScales > 0 && scaleItem != null; i++) {
            if (i == slots.knife() || i == slots.fish() || !remaining.get(i).isEmpty()) {
                continue;
            }
            int amount = Math.min(remainingScales, 64);
            remaining.set(i, new ItemStack(scaleItem, amount));
            remainingScales -= amount;
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<FishScaleRecipe> getSerializer() {
        return ModRecipeSerializers.FISH_SCALE.get();
    }

    private static int getScaleAmount(int weight) {
        return Math.max(1, (int) Math.cbrt(Math.cbrt(Math.pow(weight, 4))));
    }

    @Nullable
    private static FishData getFishData(ItemStack stack) {
        String fishId = FishStackUtil.getFishId(stack);
        if (fishId != null) {
            return FishDataRegistry.get(fishId);
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return FishDataRegistry.all().stream().filter(data -> data.itemId().equals(itemId.toString())).findFirst().orElse(null);
    }

    @Nullable
    private static Slots findSlots(CraftingInput container) {
        int knifeSlot = -1;
        int fishSlot = -1;
        List<Integer> occupiedSlots = new ArrayList<>();
        for (int i = 0; i < container.size(); i++) {
            if (!container.getItem(i).isEmpty()) {
                occupiedSlots.add(i);
            }
        }
        if (occupiedSlots.size() != 2) {
            return null;
        }

        for (int slot : occupiedSlots) {
            ItemStack stack = container.getItem(slot);
            if (stack.getItem() instanceof KnifeItem knifeItem && knifeItem.isScalingKnife() && stack.getDamageValue() < stack.getMaxDamage()) {
                knifeSlot = slot;
            } else {
                FishData fishData = getFishData(stack);
                if (fishData == null || !fishData.allowScaling() || fishData.scalingItem().isEmpty() || (FishStackUtil.isBetterFish(stack) && !FishStackUtil.hasScale(stack))) {
                    return null;
                }
                fishSlot = slot;
            }
        }
        return knifeSlot != -1 && fishSlot != -1 ? new Slots(knifeSlot, fishSlot) : null;
    }

    private record Slots(int knife, int fish) {
    }
}
