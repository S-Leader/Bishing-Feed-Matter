package net.theawesomegem.fishingmadebetter.registry;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.recipe.BaitBucketRecipe;
import net.theawesomegem.fishingmadebetter.common.recipe.BaitedRodRecipe;
import net.theawesomegem.fishingmadebetter.common.recipe.FishScaleRecipe;
import net.theawesomegem.fishingmadebetter.common.recipe.FishBucketRecipe;
import net.theawesomegem.fishingmadebetter.common.recipe.FishSliceRecipe;
import net.theawesomegem.fishingmadebetter.common.recipe.RodAttachmentRecipe;

public final class ModRecipeSerializers {
    public static final ResourceLocation ROD_ATTACHMENT_ID = new ResourceLocation(Constants.MOD_ID, "rod_attachment");
    public static final ResourceLocation BAITED_ROD_ID = new ResourceLocation(Constants.MOD_ID, "baited_rod");
    public static final ResourceLocation BAIT_BUCKET_ID = new ResourceLocation(Constants.MOD_ID, "bait_bucket");
    public static final ResourceLocation FISH_BUCKET_ID = new ResourceLocation(Constants.MOD_ID, "fish_bucket");
    public static final ResourceLocation FISH_SLICE_ID = new ResourceLocation(Constants.MOD_ID, "fish_slice");
    public static final ResourceLocation FISH_SCALE_ID = new ResourceLocation(Constants.MOD_ID, "fish_scale");
    public static Supplier<RecipeSerializer<RodAttachmentRecipe>> ROD_ATTACHMENT =
            () -> new SimpleCraftingRecipeSerializer<>(RodAttachmentRecipe::new);
    public static Supplier<RecipeSerializer<BaitedRodRecipe>> BAITED_ROD =
            () -> new SimpleCraftingRecipeSerializer<>(BaitedRodRecipe::new);
    public static Supplier<RecipeSerializer<BaitBucketRecipe>> BAIT_BUCKET =
            () -> new SimpleCraftingRecipeSerializer<>(BaitBucketRecipe::new);
    public static Supplier<RecipeSerializer<FishBucketRecipe>> FISH_BUCKET =
            () -> new SimpleCraftingRecipeSerializer<>(FishBucketRecipe::new);
    public static Supplier<RecipeSerializer<FishSliceRecipe>> FISH_SLICE =
            () -> new SimpleCraftingRecipeSerializer<>(FishSliceRecipe::new);
    public static Supplier<RecipeSerializer<FishScaleRecipe>> FISH_SCALE =
            () -> new SimpleCraftingRecipeSerializer<>(FishScaleRecipe::new);

    private ModRecipeSerializers() {
    }
}
