package net.theawesomegem.fishingmadebetter.common.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;
import org.jspecify.annotations.Nullable;

public class BaitedRodRecipe extends CustomRecipe {

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

        ItemStack rod = container.getItem(slots.rod()).copy();
        ItemStack bait = container.getItem(slots.bait());
        Identifier baitId = BuiltInRegistries.ITEM.getKey(bait.getItem());
        BetterFishingRodItem.setBaitItem(rod, baitId.toString());
        BetterFishingRodItem.setBaitMetadata(rod, 0);
        BetterFishingRodItem.setBaitDisplayName(rod, bait.getHoverName().getString());
        return rod;
    }

    @Override
    public RecipeSerializer<BaitedRodRecipe> getSerializer() {
        return ModRecipeSerializers.BAITED_ROD.get();
    }

    @Nullable
    private static Slots findSlots(CraftingInput container) {
        int rod = -1;
        int bait = -1;
        int rods = 0;
        int baits = 0;

        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof BetterFishingRodItem) {
                rods++;
                rod = i;
            } else if (BaitUtil.isValidBait(stack)) {
                baits++;
                bait = i;
            } else {
                return null;
            }
        }

        if (rods == 1 && baits == 1) {
            return new Slots(rod, bait);
        }
        return null;
    }

    private record Slots(int rod, int bait) {
    }
}
