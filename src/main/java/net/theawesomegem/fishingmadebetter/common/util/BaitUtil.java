package net.theawesomegem.fishingmadebetter.common.util;

import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;

public final class BaitUtil {
    private static final Set<String> LEGACY_BAIT_GROUPS = Set.of(
            "fruit",
            "grain",
            "vegetable",
            "meat",
            "meat_normal",
            "meatnormal",
            "meat_extra",
            "meatextra",
            "small_predator_aqc",
            "smallpredatoraqc",
            "large_predator_aqc",
            "largepredatoraqc",
            "ocean_predator_aqc",
            "oceanpredatoraqc",
            "herbivore_aqc",
            "herbivoreaqc"
    );

    private BaitUtil() {
    }

    public static boolean isValidBait(ItemStack stack) {
        return isPotentialBait(stack)
                && FishDataRegistry.all().stream().anyMatch(fishData -> isValidBaitForFish(stack, fishData));
    }

    public static boolean isPotentialBait(ItemStack stack) {
        return !stack.isEmpty()
                && !stack.is(Items.PUFFERFISH)
                && (stack.isEdible() || isLegacyBaitItem(getBaitId(stack)));
    }

    public static boolean isValidBaitForFish(ItemStack stack, FishData fishData) {
        return isPotentialBait(stack) && isValidBaitIdForFish(getBaitId(stack), fishData);
    }

    public static boolean isValidBaitIdForFish(String baitId, FishData fishData) {
        if (baitId == null || baitId.isEmpty()) {
            return false;
        }
        return fishData.validBaits().isEmpty()
                || fishData.validBaits().stream().anyMatch(validBait -> matchesBait(baitId, validBait));
    }

    public static String getBaitId(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    private static boolean matchesBait(String baitId, String validBait) {
        String normalized = validBait.toLowerCase(Locale.ROOT);
        if (baitId.equals(normalized) || baitId.equals(validBait)) {
            return true;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(baitId);
        if (parsed == null) {
            return false;
        }
        ItemStack stack = BuiltInRegistries.ITEM.get(parsed).getDefaultInstance();
        return switch (normalized) {
            case "fruit" -> stack.is(Items.APPLE)
                    || stack.is(Items.MELON_SLICE)
                    || stack.is(Items.SWEET_BERRIES)
                    || stack.is(Items.GLOW_BERRIES);
            case "grain" -> stack.is(Items.WHEAT_SEEDS)
                    || stack.is(Items.PUMPKIN_SEEDS)
                    || stack.is(Items.MELON_SEEDS)
                    || stack.is(Items.BEETROOT_SEEDS);
            case "vegetable", "herbivore_aqc", "herbivoreaqc" -> stack.is(Items.CARROT)
                    || stack.is(Items.POTATO)
                    || stack.is(Items.BEETROOT);
            case "meat_normal", "meatnormal", "small_predator_aqc", "smallpredatoraqc" -> stack.is(ItemTags.FISHES)
                    || stack.is(Items.SPIDER_EYE)
                    || stack.is(Items.ROTTEN_FLESH);
            case "meat_extra", "meatextra" -> stack.is(Items.BEEF)
                    || stack.is(Items.PORKCHOP)
                    || stack.is(Items.CHICKEN)
                    || stack.is(Items.MUTTON)
                    || stack.is(Items.RABBIT);
            case "meat", "large_predator_aqc", "largepredatoraqc", "ocean_predator_aqc", "oceanpredatoraqc" -> stack.is(ItemTags.FISHES)
                    || stack.is(Items.SPIDER_EYE)
                    || stack.is(Items.ROTTEN_FLESH)
                    || stack.is(Items.BEEF)
                    || stack.is(Items.PORKCHOP)
                    || stack.is(Items.CHICKEN)
                    || stack.is(Items.MUTTON)
                    || stack.is(Items.RABBIT);
            default -> false;
        };
    }

    private static boolean isLegacyBaitItem(String baitId) {
        return LEGACY_BAIT_GROUPS.stream().anyMatch(group -> matchesBait(baitId, group));
    }
}
