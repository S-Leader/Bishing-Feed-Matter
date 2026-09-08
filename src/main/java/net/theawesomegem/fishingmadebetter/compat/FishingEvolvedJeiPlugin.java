package net.theawesomegem.fishingmadebetter.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.fml.ModList;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.BaitBucketItem;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.FishBucketItem;
import net.theawesomegem.fishingmadebetter.common.item.KnifeItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;

@JeiPlugin
public final class FishingEvolvedJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(Constants.MOD_ID, "jei");
    public static final RecipeType<FishData> FISH_REQUIREMENTS = RecipeType.create(Constants.MOD_ID, "fish_requirements", FishData.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        List<Item> nbtItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof BetterFishingRodItem || item instanceof FishBucketItem || item instanceof BaitBucketItem)
                .toList();
        registration.useNbtForSubtypes(nbtItems.toArray(Item[]::new));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FishRequirementsRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CraftingRecipe> recipes = new ArrayList<>();
        addFishProcessingRecipes(recipes);
        addRodRecipes(recipes);
        addContainerRecipes(recipes);
        registration.addRecipes(RecipeTypes.CRAFTING, recipes);
        registration.addRecipes(FISH_REQUIREMENTS, new ArrayList<>(FishDataRegistry.all()));
    }

    @Override
    public void registerRuntime(IRuntimeRegistration registration) {
        if (ModList.get().isLoaded("aquaculture")) {
            registration.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, AquacultureCompat.disabledStacks());
        }
    }

    private static void addFishProcessingRecipes(List<CraftingRecipe> recipes) {
        Ingredient filletKnives = itemsOf(KnifeItem.class, true);
        Ingredient scalingKnives = itemsOf(KnifeItem.class, false);
        for (FishData fish : FishDataRegistry.all()) {
            Item fishItem = FishDataRegistry.resolveItem(fish.itemId());
            if (fishItem == null) {
                continue;
            }
            int weight = Math.max(1, (fish.minWeight() + fish.maxWeight()) / 2);
            ItemStack input = FishStackUtil.createCaughtFishStack(fishItem, fish, 1, weight, 0L);
            String path = safe(fish.fishId());

            if (fish.allowFillet() && !filletKnives.isEmpty()) {
                Item resultItem = fish.defaultFillet()
                        ? item("fish_slice_raw")
                        : FishDataRegistry.resolveItem(fish.filletItem());
                if (resultItem != null) {
                    recipes.add(recipe("jei/fillet/" + path, new ItemStack(resultItem), filletKnives, Ingredient.of(input)));
                }
            }
            if (fish.allowScaling() && !fish.scalingItem().isEmpty() && !scalingKnives.isEmpty()) {
                Item resultItem = FishDataRegistry.resolveItem(fish.scalingItem());
                if (resultItem != null) {
                    recipes.add(recipe("jei/scaling/" + path, new ItemStack(resultItem), scalingKnives, Ingredient.of(input)));
                }
            }
        }
    }

    private static void addRodRecipes(List<CraftingRecipe> recipes) {
        List<Item> rods = BuiltInRegistries.ITEM.stream().filter(item -> item instanceof BetterFishingRodItem).toList();
        List<Item> attachments = BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof ReelItem || item instanceof BobberItem || item instanceof HookItem)
                .toList();
        for (Item rodItem : rods) {
            String rodPath = BuiltInRegistries.ITEM.getKey(rodItem).getPath();
            for (Item attachment : attachments) {
                ItemStack output = rodItem.getDefaultInstance();
                if (attachment instanceof ReelItem reel) {
                    BetterFishingRodItem.setReelItem(output, reel);
                } else if (attachment instanceof BobberItem bobber) {
                    BetterFishingRodItem.setBobberItem(output, bobber);
                } else if (attachment instanceof HookItem hook) {
                    BetterFishingRodItem.setHookItem(output, hook);
                }
                recipes.add(recipe("jei/rod_attachment/" + rodPath + "/" + BuiltInRegistries.ITEM.getKey(attachment).getPath(),
                        output, Ingredient.of(rodItem), Ingredient.of(attachment)));
            }

            ItemStack baited = rodItem.getDefaultInstance();
            BetterFishingRodItem.setBaitItem(baited, "minecraft:carrot");
            BetterFishingRodItem.setBaitDisplayName(baited, Items.CARROT.getDescription().getString());
            recipes.add(recipe("jei/bait/" + rodPath, baited, Ingredient.of(rodItem), Ingredient.of(Items.CARROT, Items.APPLE, Items.SALMON)));
        }
    }

    private static void addContainerRecipes(List<CraftingRecipe> recipes) {
        Item baitBucketItem = item("bait_bucket");
        if (baitBucketItem instanceof BaitBucketItem) {
            ItemStack baitBucket = baitBucketItem.getDefaultInstance();
            BaitBucketItem.setBaitId(baitBucket, "minecraft:carrot");
            BaitBucketItem.setBaitDisplayName(baitBucket, Items.CARROT.getDescription().getString());
            BaitBucketItem.setBaitCount(baitBucket, 1);
            recipes.add(recipe("jei/bait_bucket", baitBucket, Ingredient.of(baitBucketItem), Ingredient.of(Items.CARROT)));
        }

        Item fishBucketItem = item("fish_bucket");
        if (fishBucketItem instanceof FishBucketItem fishBucket) {
            FishData fish = FishDataRegistry.all().stream().filter(data -> data.liquid() == FishData.FishingLiquid.WATER).findFirst().orElse(null);
            Item source = fish == null ? null : FishDataRegistry.resolveItem(fish.itemId());
            if (fish != null && source != null) {
                ItemStack caught = FishStackUtil.createCaughtFishStack(source, fish, 1, fish.minWeight(), 0L);
                recipes.add(recipe("jei/fish_bucket", fishBucket.createStack(fish.fishId()), Ingredient.of(Items.WATER_BUCKET), Ingredient.of(caught)));
            }
        }
    }

    private static Ingredient itemsOf(Class<? extends KnifeItem> type, boolean fillet) {
        ItemStack[] stacks = BuiltInRegistries.ITEM.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .filter(knife -> fillet ? knife.isFilletKnife() : knife.isScalingKnife())
                .map(Item::getDefaultInstance)
                .toArray(ItemStack[]::new);
        return stacks.length == 0 ? Ingredient.EMPTY : Ingredient.of(stacks);
    }

    private static ShapelessRecipe recipe(String path, ItemStack result, Ingredient... ingredients) {
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, ingredients);
        return new ShapelessRecipe(new ResourceLocation(Constants.MOD_ID, path), "fishingmadebetter.jei", CraftingBookCategory.MISC, result, inputs);
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(Constants.MOD_ID, path));
    }

    private static String safe(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
    }
}
