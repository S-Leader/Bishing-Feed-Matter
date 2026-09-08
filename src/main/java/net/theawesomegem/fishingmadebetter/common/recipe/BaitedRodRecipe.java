package net.theawesomegem.fishingmadebetter.common.recipe;

import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import net.theawesomegem.fishingmadebetter.registry.ModRecipeSerializers;

public class BaitedRodRecipe extends CustomRecipe {
    public BaitedRodRecipe(ResourceLocation id, CraftingBookCategory category) {
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

        ItemStack rod = container.getItem(slots.rod()).copy();
        ItemStack bait = container.getItem(slots.bait());
        ResourceLocation baitId = BuiltInRegistries.ITEM.getKey(bait.getItem());
        BetterFishingRodItem.setBaitItem(rod, baitId.toString());
        BetterFishingRodItem.setBaitMetadata(rod, 0);
        BetterFishingRodItem.setBaitDisplayName(rod, bait.getHoverName().getString());
        return rod;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.BAITED_ROD.get();
    }

    @Nullable
    private static Slots findSlots(CraftingContainer container) {
        int rod = -1;
        int bait = -1;
        int rods = 0;
        int baits = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
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
