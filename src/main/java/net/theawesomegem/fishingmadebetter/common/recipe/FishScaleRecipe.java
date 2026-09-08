package net.theawesomegem.fishingmadebetter.common.recipe;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.KnifeItem;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;

public class FishScaleRecipe extends CustomRecipe {
    public FishScaleRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findSlots(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
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

        int amount = fishData.scalingUseWeight()
                ? Math.min(getScaleAmount(FishStackUtil.getFishWeight(container.getItem(slots.fish()))), 64)
                : 1;
        return new ItemStack(item, amount);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        Slots slots = findSlots(container);
        if (slots == null) {
            return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        }

        ItemStack fish = container.getItem(slots.fish());
        FishData fishData = getFishData(fish);
        int totalScales = fishData != null && fishData.scalingUseWeight() ? getScaleAmount(FishStackUtil.getFishWeight(fish)) : 1;
        int remainingScales = Math.max(0, totalScales - 64);

        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        ItemStack knife = container.getItem(slots.knife()).copy();
        knife.setDamageValue(knife.getDamageValue() + totalScales);
        if (knife.getDamageValue() < knife.getMaxDamage()) {
            remaining.set(slots.knife(), knife);
        }

        ItemStack scaledFish = fish.copy();
        scaledFish.setCount(1);
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FISH_SCALE.get();
    }

    private static int getScaleAmount(int weight) {
        return Math.max(1, (int) Math.cbrt(Math.cbrt(Math.pow(weight, 4))));
    }

    @Nullable
    private static FishData getFishData(ItemStack stack) {
        String fishId = FishStackUtil.getFishId(stack);
        return fishId == null ? null : FishDataRegistry.get(fishId);
    }

    @Nullable
    private static Slots findSlots(CraftingContainer container) {
        int knifeSlot = -1;
        int fishSlot = -1;
        List<Integer> occupiedSlots = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
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
            } else if (FishStackUtil.isBetterFish(stack) && FishStackUtil.hasScale(stack)) {
                FishData fishData = getFishData(stack);
                if (fishData == null || !fishData.allowScaling() || fishData.scalingItem().isEmpty()) {
                    return null;
                }
                fishSlot = slot;
            } else {
                return null;
            }
        }
        return knifeSlot != -1 && fishSlot != -1 ? new Slots(knifeSlot, fishSlot) : null;
    }

    private record Slots(int knife, int fish) {
    }
}
