package net.theawesomegem.fishingmadebetter.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.recipe.*;

import java.util.function.Supplier;

public final class ModRecipeSerializers {
    public static final Identifier ROD_ATTACHMENT_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "rod_attachment");
    public static final Identifier BAITED_ROD_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "baited_rod");
    public static final Identifier BAIT_BUCKET_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "bait_bucket");
    public static final Identifier FISH_BUCKET_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fish_bucket");
    public static final Identifier FISH_SLICE_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fish_slice");
    public static final Identifier FISH_SCALE_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "fish_scale");
    public static Supplier<RecipeSerializer<RodAttachmentRecipe>> ROD_ATTACHMENT = () -> new RecipeSerializer<>(MapCodec.unit(RodAttachmentRecipe::new), StreamCodec.of((buffer, recipe) -> {
    }, buffer -> new RodAttachmentRecipe()));
    public static Supplier<RecipeSerializer<BaitedRodRecipe>> BAITED_ROD = () -> new RecipeSerializer<>(MapCodec.unit(BaitedRodRecipe::new), StreamCodec.of((buffer, recipe) -> {
    }, buffer -> new BaitedRodRecipe()));
    public static Supplier<RecipeSerializer<BaitBucketRecipe>> BAIT_BUCKET = () -> new RecipeSerializer<>(MapCodec.unit(BaitBucketRecipe::new), StreamCodec.of((buffer, recipe) -> {
    }, buffer -> new BaitBucketRecipe()));
    public static Supplier<RecipeSerializer<FishBucketRecipe>> FISH_BUCKET = () -> new RecipeSerializer<>(MapCodec.unit(FishBucketRecipe::new), StreamCodec.of((buffer, recipe) -> {
    }, buffer -> new FishBucketRecipe()));
    public static Supplier<RecipeSerializer<FishSliceRecipe>> FISH_SLICE = () -> new RecipeSerializer<>(MapCodec.unit(FishSliceRecipe::new), StreamCodec.of((buffer, recipe) -> {
    }, buffer -> new FishSliceRecipe()));
    public static Supplier<RecipeSerializer<FishScaleRecipe>> FISH_SCALE = () -> new RecipeSerializer<>(MapCodec.unit(FishScaleRecipe::new), StreamCodec.of((buffer, recipe) -> {
    }, buffer -> new FishScaleRecipe()));

    private ModRecipeSerializers() {
    }
}
