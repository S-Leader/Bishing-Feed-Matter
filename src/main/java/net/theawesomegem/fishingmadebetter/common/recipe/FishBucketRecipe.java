package net.theawesomegem.fishingmadebetter.common.recipe;

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
import net.theawesomegem.fishingmadebetter.common.data.FishData.FishingLiquid;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.FishBucketItem;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FishBucketRecipe extends CustomRecipe {
    private static final Identifier FISH_BUCKET_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fish_bucket");

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return findSlots(container, level.getGameTime()) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        Slots slots = findSlots(container, -1L);
        if (slots == null) {
            return ItemStack.EMPTY;
        }

        String fishId = FishStackUtil.getFishId(container.getItem(slots.fish()));
        Item item = BuiltInRegistries.ITEM.getValue(FISH_BUCKET_ID);
        if (fishId == null || !(item instanceof FishBucketItem fishBucketItem)) {
            return ItemStack.EMPTY;
        }
        return fishBucketItem.createStack(fishId);
    }

    @Override
    public RecipeSerializer<FishBucketRecipe> getSerializer() {
        return ModRecipeSerializers.FISH_BUCKET.get();
    }

    @Nullable
    private static Slots findSlots(CraftingInput container, long currentTime) {
        int bucket = -1;
        int fish = -1;
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
            if (stack.is(Items.WATER_BUCKET)) {
                bucket = slot;
            } else if (isBucketableFish(stack, currentTime)) {
                fish = slot;
            } else {
                return null;
            }
        }
        return bucket != -1 && fish != -1 ? new Slots(bucket, fish) : null;
    }

    private static boolean isBucketableFish(ItemStack stack, long currentTime) {
        String fishId = FishStackUtil.getFishId(stack);
        FishData fishData = fishId == null ? null : FishDataRegistry.get(fishId);
        return FishStackUtil.isBetterFish(stack) && fishData != null && (currentTime < 0L ? FishStackUtil.getFishCaughtTime(stack) != 0L : !FishStackUtil.isDead(stack, currentTime)) && (fishData.liquid() == FishingLiquid.WATER || fishData.liquid() == FishingLiquid.ANY);
    }

    private record Slots(int bucket, int fish) {
    }
}
