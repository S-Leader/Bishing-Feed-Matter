package net.theawesomegem.fishingmadebetter.common.recipe;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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

public class FishBucketRecipe extends CustomRecipe {
    private static final ResourceLocation FISH_BUCKET_ID = new ResourceLocation(Constants.MOD_ID, "fish_bucket");

    public FishBucketRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findSlots(container, level.getGameTime()) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        Slots slots = findSlots(container, -1L);
        if (slots == null) {
            return ItemStack.EMPTY;
        }

        String fishId = FishStackUtil.getFishId(container.getItem(slots.fish()));
        Item item = BuiltInRegistries.ITEM.get(FISH_BUCKET_ID);
        if (fishId == null || !(item instanceof FishBucketItem fishBucketItem)) {
            return ItemStack.EMPTY;
        }
        return fishBucketItem.createStack(fishId);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FISH_BUCKET.get();
    }

    @Nullable
    private static Slots findSlots(CraftingContainer container, long currentTime) {
        int bucket = -1;
        int fish = -1;
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
        return FishStackUtil.isBetterFish(stack)
                && fishData != null
                && (currentTime < 0L ? FishStackUtil.getFishCaughtTime(stack) != 0L : !FishStackUtil.isDead(stack, currentTime))
                && (fishData.liquid() == FishingLiquid.WATER || fishData.liquid() == FishingLiquid.ANY);
    }

    private record Slots(int bucket, int fish) {
    }
}
