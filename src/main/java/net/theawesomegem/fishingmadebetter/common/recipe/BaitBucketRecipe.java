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
import net.theawesomegem.fishingmadebetter.common.item.BaitBucketItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BaitBucketRecipe extends CustomRecipe {
    private static final int MAX_BAIT_COUNT = 64;

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

        ItemStack bucket = container.getItem(slots.bucket()).copy();
        if (slots.baitSlots().isEmpty()) {
            BaitBucketItem.removeBait(bucket);
            return bucket;
        }

        ItemStack bait = container.getItem(slots.baitSlots().get(0));
        Identifier baitId = BuiltInRegistries.ITEM.getKey(bait.getItem());
        BaitBucketItem.setBaitId(bucket, baitId.toString());
        BaitBucketItem.setBaitMetadata(bucket, 0);
        BaitBucketItem.setBaitDisplayName(bucket, bait.getHoverName().getString());
        BaitBucketItem.setBaitCount(bucket, BaitBucketItem.getBaitCount(bucket) + slots.baitSlots().size());
        return bucket;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.size(), ItemStack.EMPTY);
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
    public RecipeSerializer<BaitBucketRecipe> getSerializer() {
        return ModRecipeSerializers.BAIT_BUCKET.get();
    }

    @Nullable
    private static Slots findSlots(CraftingInput container) {
        int bucket = -1;
        List<Integer> baitSlots = new ArrayList<>();
        Item baitItem = null;
        int occupied = 0;

        for (int i = 0; i < container.size(); i++) {
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

        Identifier baitId = BuiltInRegistries.ITEM.getKey(baitItem);
        if (!storedBait.isEmpty() && !storedBait.equals(baitId.toString())) {
            return null;
        }
        if (storedCount + baitSlots.size() > MAX_BAIT_COUNT) {
            return null;
        }
        return new Slots(bucket, baitSlots);
    }

    private static Item resolveStoredBait(ItemStack bucket) {
        Identifier baitId = Identifier.tryParse(BaitBucketItem.getBaitId(bucket));
        if (baitId == null) {
            return Items.AIR;
        }
        return BuiltInRegistries.ITEM.getValue(baitId);
    }

    private record Slots(int bucket, List<Integer> baitSlots) {
    }
}
