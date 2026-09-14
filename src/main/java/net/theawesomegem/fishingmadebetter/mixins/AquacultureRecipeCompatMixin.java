package net.theawesomegem.fishingmadebetter.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Removes disabled Aquaculture knife/hook recipes and redirects any JSON fillet output to FMB.
 */
@Mixin(RecipeManager.class)
public abstract class AquacultureRecipeCompatMixin {
    private static final Set<ResourceLocation> DISABLED_RECIPES = Set.of(
            aquaculture("wooden_fillet_knife"),
            aquaculture("stone_fillet_knife"),
            aquaculture("iron_fillet_knife"),
            aquaculture("gold_fillet_knife"),
            aquaculture("diamond_fillet_knife"),
            aquaculture("neptunium_fillet_knife"),
            aquaculture("iron_hook"),
            aquaculture("gold_hook"),
            aquaculture("diamond_hook"),
            aquaculture("light_hook"),
            aquaculture("heavy_hook"),
            aquaculture("double_hook"),
            aquaculture("redstone_hook"),
            aquaculture("note_hook"),
            aquaculture("nether_star_hook")
    );

    @ModifyVariable(method = "apply", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Map<ResourceLocation, JsonElement> fishingmadebetter$rewriteAquacultureRecipes(
            Map<ResourceLocation, JsonElement> recipes
    ) {
        Map<ResourceLocation, JsonElement> rewritten = new LinkedHashMap<>(recipes.size());
        for (Map.Entry<ResourceLocation, JsonElement> entry : recipes.entrySet()) {
            if (DISABLED_RECIPES.contains(entry.getKey())) {
                continue;
            }

            JsonElement json = entry.getValue().deepCopy();
            redirectFilletResult(json);
            rewritten.put(entry.getKey(), json);
        }
        return rewritten;
    }

    private static void redirectFilletResult(JsonElement json) {
        if (!json.isJsonObject()) {
            return;
        }

        JsonObject recipe = json.getAsJsonObject();
        JsonElement result = recipe.get("result");
        if (result == null) {
            return;
        }

        if (result.isJsonPrimitive() && result.getAsJsonPrimitive().isString()) {
            String replacement = replacementFor(result.getAsString());
            if (replacement != null) {
                recipe.addProperty("result", replacement);
            }
            return;
        }

        if (result.isJsonObject()) {
            JsonObject resultObject = result.getAsJsonObject();
            if (resultObject.has("item")) {
                String replacement = replacementFor(resultObject.get("item").getAsString());
                if (replacement != null) {
                    resultObject.addProperty("item", replacement);
                }
            }
        }
    }

    private static String replacementFor(String itemId) {
        return switch (itemId) {
            case "aquaculture:fish_fillet_raw" -> "fishingmadebetter:fish_slice_raw";
            case "aquaculture:fish_fillet_cooked" -> "fishingmadebetter:fish_slice_cooked";
            default -> null;
        };
    }

    private static ResourceLocation aquaculture(String path) {
        return new ResourceLocation("aquaculture", path);
    }
}
