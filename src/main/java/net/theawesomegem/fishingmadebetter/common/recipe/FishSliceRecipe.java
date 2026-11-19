package net.theawesomegem.fishingmadebetter.common.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.FishSliceItem;
import net.theawesomegem.fishingmadebetter.common.item.KnifeItem;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FishSliceRecipe extends CustomRecipe {
    private static final Identifier RAW_SLICE_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fish_slice_raw");

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

        ItemStack fish = container.getItem(slots.fish());
        FishData fishData = getFishData(fish);
        int amount = fishData != null && fishData.filletUseWeight() ? Math.min(getSliceAmount(FishStackUtil.getFishWeight(fish)), 64) : 1;
        return createSliceStack(fish, amount);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        Slots slots = findSlots(container);
        if (slots == null) {
            return NonNullList.withSize(container.size(), ItemStack.EMPTY);
        }

        ItemStack fish = container.getItem(slots.fish());
        FishData fishData = getFishData(fish);
        int totalSlices = fishData != null && fishData.filletUseWeight() ? getSliceAmount(FishStackUtil.getFishWeight(fish)) : 1;
        int remainingSlices = Math.max(0, totalSlices - 64);

        NonNullList<ItemStack> remaining = NonNullList.withSize(container.size(), ItemStack.EMPTY);
        ItemStack knife = container.getItem(slots.knife()).copy();
        knife.setDamageValue(knife.getDamageValue() + totalSlices);
        if (knife.getDamageValue() < knife.getMaxDamage()) {
            remaining.set(slots.knife(), knife);
        }

        for (int i = 0; i < remaining.size() && remainingSlices > 0; i++) {
            if (i == slots.knife() || i == slots.fish() || !remaining.get(i).isEmpty()) {
                continue;
            }
            int amount = Math.min(remainingSlices, 64);
            remaining.set(i, createSliceStack(fish, amount));
            remainingSlices -= amount;
        }
        return remaining;
    }

    private static ItemStack createSliceStack(ItemStack fish, int amount) {
        FishData fishData = getFishData(fish);
        if (fishData != null && !fishData.defaultFillet() && !fishData.filletItem().isEmpty()) {
            Item customFillet = FishDataRegistry.resolveItem(fishData.filletItem());
            return customFillet == null ? ItemStack.EMPTY : new ItemStack(customFillet, amount);
        }

        Item item = BuiltInRegistries.ITEM.getValue(RAW_SLICE_ID);
        if (item instanceof FishSliceItem sliceItem) {
            if (FishStackUtil.isBetterFish(fish)) {
                String fishId = FishStackUtil.getFishId(fish);
                return sliceItem.createStack(fishId, fishId, amount);
            }
            return new ItemStack(sliceItem, amount);
        }
        return ItemStack.EMPTY;
    }

    private static int getSliceAmount(int weight) {
        return Math.max(1, (int) Math.cbrt(Math.pow(weight, 2)));
    }

    @Override
    public RecipeSerializer<FishSliceRecipe> getSerializer() {
        return ModRecipeSerializers.FISH_SLICE.get();
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
            if (stack.getItem() instanceof KnifeItem knifeItem && knifeItem.isFilletKnife() && stack.getDamageValue() < stack.getMaxDamage()) {
                knifeSlot = slot;
            } else if (isSliceableFish(stack)) {
                fishSlot = slot;
            } else {
                return null;
            }
        }
        return knifeSlot != -1 && fishSlot != -1 ? new Slots(knifeSlot, fishSlot) : null;
    }

    private static boolean isVanillaRawFish(ItemStack stack) {
        return stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH);
    }

    private static boolean isSliceableFish(ItemStack stack) {
        FishData fishData = getFishData(stack);
        return (fishData != null && fishData.allowFillet()) || isVanillaRawFish(stack);
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

    private record Slots(int knife, int fish) {
    }
}
