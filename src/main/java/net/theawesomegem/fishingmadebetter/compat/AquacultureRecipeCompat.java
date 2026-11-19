package net.theawesomegem.fishingmadebetter.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;

import java.util.Set;

/**
 * Removes disabled Aquaculture knife/hook recipes and redirects any JSON fillet output to FMB.
 */
@EventBusSubscriber(modid = "fishingmadebetter")
public final class AquacultureRecipeCompat {
    private static final Set<Identifier> DISABLED_RECIPES =
            Set.of(
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
                    aquaculture("nether_star_hook"));

    @SubscribeEvent
    public static void rewriteRecipes(ModifyRecipeJsonsEvent event) {
        if (!ModList.get().isLoaded("aquaculture")) return;
        event.getRecipeJsons().keySet().removeAll(DISABLED_RECIPES);
        event.getRecipeJsons().values().forEach(AquacultureRecipeCompat::redirectFilletResult);
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
            if (resultObject.has("id")) {
                String replacement = replacementFor(resultObject.get("id").getAsString());
                if (replacement != null) {
                    resultObject.addProperty("id", replacement);
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

    private static Identifier aquaculture(String path) {
        return Identifier.fromNamespaceAndPath("aquaculture", path);
    }
}
