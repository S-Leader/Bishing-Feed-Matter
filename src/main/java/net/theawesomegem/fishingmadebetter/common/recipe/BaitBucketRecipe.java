package net.theawesomegem.fishingmadebetter.common.recipe;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
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
import net.theawesomegem.fishingmadebetter.common.item.BaitBucketItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;

public class BaitBucketRecipe extends CustomRecipe {
    private static final int MAX_BAIT_COUNT = 64;

    public BaitBucketRecipe(ResourceLocation id, CraftingBookCategory category) {
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

        ItemStack bucket = container.getItem(slots.bucket()).copy();
        if (slots.baitSlots().isEmpty()) {
            BaitBucketItem.removeBait(bucket);
            return bucket;
        }

        ItemStack bait = container.getItem(slots.baitSlots().get(0));
        ResourceLocation baitId = BuiltInRegistries.ITEM.getKey(bait.getItem());
        BaitBucketItem.setBaitId(bucket, baitId.toString());
        BaitBucketItem.setBaitMetadata(bucket, 0);
        BaitBucketItem.setBaitDisplayName(bucket, bait.getHoverName().getString());
        BaitBucketItem.setBaitCount(bucket, BaitBucketItem.getBaitCount(bucket) + slots.baitSlots().size());
        return bucket;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        Slots slots = findSlots(container);
        if (slots == null || !slots.baitSlots().isEmpty()) {
            return remaining;
        }

        ItemStack bucket = container.getItem(slots.bucket());
        Item baitItem = resolveStoredBait(bucket);
        int count = BaitBucketItem.getBaitCount(bucket);
        if (baitItem != Items.AIR && count > 0) {
            remaining.set(slots.bucket(), new ItemStack(baitItem, count));
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.BAIT_BUCKET.get();
    }

    @Nullable
    private static Slots findSlots(CraftingContainer container) {
        int bucket = -1;
        List<Integer> baitSlots = new ArrayList<>();
        Item baitItem = null;
        int occupied = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            occupied++;

            if (stack.getItem() instanceof BaitBucketItem && bucket == -1) {
                bucket = i;
                continue;
            }
            if (BaitUtil.isValidBait(stack) && (baitItem == null || stack.is(baitItem))) {
                baitSlots.add(i);
                baitItem = stack.getItem();
                continue;
            }
            return null;
        }

        if (bucket == -1 || occupied == 0) {
            return null;
        }

        ItemStack bucketStack = container.getItem(bucket);
        String storedBait = BaitBucketItem.getBaitId(bucketStack);
        int storedCount = BaitBucketItem.getBaitCount(bucketStack);

        if (baitSlots.isEmpty()) {
            return !storedBait.isEmpty() && storedCount > 0 ? new Slots(bucket, baitSlots) : null;
        }

        ResourceLocation baitId = BuiltInRegistries.ITEM.getKey(baitItem);
        if (!storedBait.isEmpty() && !storedBait.equals(baitId.toString())) {
            return null;
        }
        if (storedCount + baitSlots.size() > MAX_BAIT_COUNT) {
            return null;
        }
        return new Slots(bucket, baitSlots);
    }

    private static Item resolveStoredBait(ItemStack bucket) {
        ResourceLocation baitId = ResourceLocation.tryParse(BaitBucketItem.getBaitId(bucket));
        if (baitId == null) {
            return Items.AIR;
        }
        return BuiltInRegistries.ITEM.get(baitId);
    }

    private record Slots(int bucket, List<Integer> baitSlots) {
    }
}
