package net.theawesomegem.fishingmadebetter.compat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.data.FishData;
import net.theawesomegem.fishingmadebetter.common.data.FishDataRegistry;
import net.theawesomegem.fishingmadebetter.common.item.BetterFishingRodItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.BobberItem;
import net.theawesomegem.fishingmadebetter.common.item.attachment.ReelItem;
import net.theawesomegem.fishingmadebetter.common.util.BaitUtil;
import net.theawesomegem.fishingmadebetter.common.util.FishStackUtil;

/**
 * JEI's fish-catching requirements view, ported from Localizator's 1.12.2
 * Fishing Made Better integration and adapted to the modern FishData format.
 */
public final class FishRequirementsRecipeCategory implements IRecipeCategory<FishData> {
    private static final ResourceLocation BACKGROUND_TEXTURE = id("textures/gui/fishreq_gui.png");
    private static final ResourceLocation OUTLINE_TEXTURE = id("textures/gui/reeling_hud_outline.png");
    private static final ResourceLocation LIQUID_SKY_TEXTURE = id("textures/gui/reeling_hud_underoverlay.png");
    private static final ResourceLocation BIOME_TEXTURE = id("textures/gui/reeling_hud_biome.png");
    private static final ResourceLocation DIMENSION_TEXTURE = id("textures/gui/reeling_hud_fullsize.png");
    private static final ResourceLocation OVERLAYS_TEXTURE = id("textures/gui/fish_data_overlays.png");

    private static final int X_OFFSET = -8;
    private static final int Y_OFFSET = -6;
    private static final int MINIGAME_X = 20;
    private static final int MINIGAME_Y = 5;
    private static final int MINIGAME_INNER_X = MINIGAME_X + 3;
    private static final int MINIGAME_INNER_Y = MINIGAME_Y + 3;
    private static final int YMETER_X = 8;
    private static final int YMETER_Y = 5;
    private static final int LIGHT_X = 57;
    private static final int LIGHT_Y = 50;
    private static final int MAX_Y_LEVEL_PIXELS = 28;

    private static final List<String> DEFAULT_BIOMES = List.of(
            "MUSHROOM", "DEAD", "SPOOKY", "JUNGLE", "SWAMP", "HOT",
            "SANDY", "COLD", "MOUNTAIN", "FOREST", "PLAINS", "RIVER", "WATER");

    private final IDrawable background;
    private final IDrawable icon;

    public FishRequirementsRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(BACKGROUND_TEXTURE, 7, 5, 163, 120);
        Item tracker = item("fish_tracker_diamond");
        this.icon = guiHelper.createDrawableItemStack(tracker == Items.AIR ? Items.FISHING_ROD.getDefaultInstance() : tracker.getDefaultInstance());
    }

    @Override
    public RecipeType<FishData> getRecipeType() {
        return FishingEvolvedJeiPlugin.FISH_REQUIREMENTS;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.fishingmadebetter.category.fish_requirements");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getRegistryName(FishData recipe) {
        return id("fish_requirements/" + recipe.fishId().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FishData recipe, IFocusGroup focuses) {
        Item fishItem = FishDataRegistry.resolveItem(recipe.itemId());
        if (fishItem != null && fishItem != Items.AIR) {
            int weight = Math.max(1, (recipe.minWeight() + recipe.maxWeight()) / 2);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 23 + X_OFFSET, 55 + Y_OFFSET)
                    .addItemStack(FishStackUtil.createCaughtFishStack(fishItem, recipe, 1, weight, 0L));
        }

        addStacks(builder, 14 + X_OFFSET, 83 + Y_OFFSET, fishingRods());
        addStacks(builder, 32 + X_OFFSET, 83 + Y_OFFSET, reels(recipe));
        addStacks(builder, 14 + X_OFFSET, 101 + Y_OFFSET, bobbers(recipe));

        Item baitBucket = item("bait_bucket");
        if (baitBucket != Items.AIR) {
            builder.addSlot(RecipeIngredientRole.INPUT, 64 + X_OFFSET, 101 + Y_OFFSET)
                    .addItemStack(baitBucket.getDefaultInstance());
        }

        List<ItemStack> baits = baits(recipe);
        for (int i = 0; i < Math.min(16, baits.size()); i++) {
            int x = 96 + X_OFFSET + (i / 4) * 18;
            int y = 51 + Y_OFFSET + (i % 4) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStack(baits.get(i));
        }
    }

    @Override
    public void draw(FishData recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        DisplayView view = currentView(recipe);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawMinigame(graphics, recipe, view);
        drawYMeter(graphics, recipe, view.dimension());
        drawLightLevel(graphics, recipe.maxLightLevel());
        RenderSystem.disableBlend();
    }

    @Override
    public List<Component> getTooltipStrings(FishData recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, YMETER_X, YMETER_Y, 7, 30)) {
            return List.of(Component.literal("Y: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(Integer.toString(recipe.minYLevel())).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(Integer.toString(recipe.maxYLevel())).withStyle(ChatFormatting.WHITE)));
        }

        if (inside(mouseX, mouseY, MINIGAME_X, MINIGAME_Y, 134, 30)) {
            DisplayView view = currentView(recipe);
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("jei.fishingmadebetter.category.fish_requirements.minigame.title")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable(
                            "jei.fishingmadebetter.category.fish_requirements.minigame.biometype",
                            biomeName(view.biomeLabel()).copy().withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                            "jei.fishingmadebetter.category.fish_requirements.minigame.time.tooltip",
                            Component.translatable("jei.fishingmadebetter.time." + recipe.timeToFish().name())
                                    .withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                            "jei.fishingmadebetter.category.fish_requirements.minigame.rain.tooltip",
                            Component.translatable("jei.fishingmadebetter.boolean." + recipe.rainRequired())
                                    .withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(
                            "jei.fishingmadebetter.category.fish_requirements.minigame.thunder.tooltip",
                            Component.translatable("jei.fishingmadebetter.boolean." + recipe.thunderRequired())
                                    .withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));
            return tooltip;
        }

        if (inside(mouseX, mouseY, LIGHT_X, LIGHT_Y, 18, 18)) {
            return List.of(Component.translatable(
                            "jei.fishingmadebetter.category.fish_requirements.minigame.light.tooltip",
                            Component.literal("0 - " + recipe.maxLightLevel()).withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.GRAY));
        }

        return List.of();
    }

    private static void drawMinigame(GuiGraphics graphics, FishData fish, DisplayView view) {
        if (view.dimension() == DisplayDimension.NETHER) {
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 24);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            blit128(graphics, DIMENSION_TEXTURE, 0, 24);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
        } else if (view.dimension() == DisplayDimension.END) {
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 48);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            blit128(graphics, DIMENSION_TEXTURE, 0, 0);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
        } else {
            int biomeTexture = biomeTexturePosition(view.biomeVisual());
            switch (fish.liquid()) {
                case LAVA -> blit128(graphics, LIQUID_SKY_TEXTURE, 0, 24);
                case VOID -> blit128(graphics, LIQUID_SKY_TEXTURE, 0, 48);
                case ANY -> blit128(graphics, OVERLAYS_TEXTURE, 128, 24);
                case WATER -> blit128(graphics, LIQUID_SKY_TEXTURE, 128, biomeTexture * 24);
            }

            switch (fish.timeToFish()) {
                case NIGHT -> {
                    blit128(graphics, LIQUID_SKY_TEXTURE, 0, 96);
                    blit128(graphics, BIOME_TEXTURE, 0, biomeTexture * 24);
                    blit128(graphics, LIQUID_SKY_TEXTURE, 0, 144);
                }
                case ANY -> {
                    blit128(graphics, OVERLAYS_TEXTURE, 128, 0);
                    blit128(graphics, BIOME_TEXTURE, 0, biomeTexture * 24);
                    blit128(graphics, OVERLAYS_TEXTURE, 128, 48);
                }
                case DAY -> {
                    blit128(graphics, LIQUID_SKY_TEXTURE, 0, 72);
                    blit128(graphics, BIOME_TEXTURE, 0, biomeTexture * 24);
                    blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
                }
            }

            if (fish.rainRequired()) {
                blit128(graphics, OVERLAYS_TEXTURE, 0, 0);
            } else {
                blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            }
            if (fish.thunderRequired()) {
                blit128(graphics, OVERLAYS_TEXTURE, 0, 24);
            } else {
                blit128(graphics, LIQUID_SKY_TEXTURE, 0, 120);
            }
        }

        graphics.blit(OUTLINE_TEXTURE, MINIGAME_X, MINIGAME_Y, 0, 0, 134, 30);
    }

    private static void drawYMeter(GuiGraphics graphics, FishData fish, DisplayDimension dimension) {
        graphics.blit(OVERLAYS_TEXTURE, YMETER_X, YMETER_Y, 0, 49, 7, 30);

        int minY = Math.max(fish.minYLevel(), 0);
        int maxY = Math.min(fish.maxYLevel(), 140);
        if (minY >= maxY) {
            minY = maxY;
        }
        int minStep = minY / 5;
        int maxStep = maxY / 5;
        int top = MAX_Y_LEVEL_PIXELS - maxStep;
        int height = Math.max(1, maxStep - minStep + 1);
        graphics.blit(OVERLAYS_TEXTURE, YMETER_X + 1, YMETER_Y + 1 + top, 16, 49 + top, 5, height);
        graphics.blit(OVERLAYS_TEXTURE, YMETER_X, YMETER_Y, 8, 49, 7, 30);

        int level;
        int levelTextureV;
        if (dimension == DisplayDimension.NETHER) {
            level = 31;
            levelTextureV = 51;
        } else if (dimension == DisplayDimension.END) {
            level = 5;
            levelTextureV = 53;
        } else {
            level = 62;
            levelTextureV = 49;
        }
        int levelY = 1 + (MAX_Y_LEVEL_PIXELS - Mth.clamp(level, 0, 140) / 5);
        graphics.blit(OVERLAYS_TEXTURE, YMETER_X - 1, YMETER_Y + levelY, 22, levelTextureV, 9, 1);
    }

    private static void drawLightLevel(GuiGraphics graphics, int maxLightLevel) {
        if (maxLightLevel < 0) {
            graphics.renderItem(Items.BARRIER.getDefaultInstance(), LIGHT_X, LIGHT_Y);
            return;
        }
        int light = Mth.clamp(maxLightLevel, 0, 15);
        ResourceLocation texture = id("textures/gui/light/" + String.format(Locale.ROOT, "%02d", light) + ".png");
        graphics.blit(texture, LIGHT_X, LIGHT_Y, 0, 0, 16, 16, 16, 16);
    }

    private static List<ItemStack> fishingRods() {
        return BuiltInRegistries.ITEM.stream()
                .filter(BetterFishingRodItem.class::isInstance)
                .map(Item::getDefaultInstance)
                .toList();
    }

    private static List<ItemStack> reels(FishData fish) {
        return BuiltInRegistries.ITEM.stream()
                .filter(ReelItem.class::isInstance)
                .map(ReelItem.class::cast)
                .filter(reel -> fish.minDeepLevel() < reel.getReelRange())
                .map(Item::getDefaultInstance)
                .toList();
    }

    private static List<ItemStack> bobbers(FishData fish) {
        return BuiltInRegistries.ITEM.stream()
                .filter(BobberItem.class::isInstance)
                .map(BobberItem.class::cast)
                .filter(bobber -> switch (fish.liquid()) {
                    case LAVA -> bobber.isLavaBobber();
                    case VOID -> bobber.isVoidBobber();
                    case WATER -> !bobber.isLavaBobber() && !bobber.isVoidBobber();
                    case ANY -> true;
                })
                .map(Item::getDefaultInstance)
                .toList();
    }

    private static List<ItemStack> baits(FishData fish) {
        return BuiltInRegistries.ITEM.stream()
                .map(Item::getDefaultInstance)
                .filter(stack -> BaitUtil.isValidBaitForFish(stack, fish))
                .limit(16)
                .toList();
    }

    private static void addStacks(IRecipeLayoutBuilder builder, int x, int y, List<ItemStack> stacks) {
        if (!stacks.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(stacks);
        }
    }

    private static DisplayView currentView(FishData fish) {
        List<DisplayView> views = views(fish);
        if (views.isEmpty()) {
            return new DisplayView(DisplayDimension.OVERWORLD, "UNKNOWN", "WATER");
        }
        Minecraft minecraft = Minecraft.getInstance();
        int ticks = minecraft.player == null ? 0 : minecraft.player.tickCount;
        return views.get((ticks / 20) % views.size());
    }

    private static List<DisplayView> views(FishData fish) {
        List<DisplayDimension> dimensions = allowedDimensions(fish);
        List<DisplayView> result = new ArrayList<>();
        for (DisplayDimension dimension : dimensions) {
            if (dimension == DisplayDimension.NETHER) {
                result.add(new DisplayView(dimension, "NETHER", "WATER"));
            } else if (dimension == DisplayDimension.END) {
                result.add(new DisplayView(dimension, "END", "WATER"));
            } else {
                List<String> biomes = allowedBiomes(fish);
                if (biomes.isEmpty()) {
                    result.add(new DisplayView(dimension, "UNKNOWN", "WATER"));
                } else {
                    for (String biome : biomes) {
                        result.add(new DisplayView(dimension, biomeLabel(biome), biomeVisual(biome)));
                    }
                }
            }
        }
        return result;
    }

    private static List<DisplayDimension> allowedDimensions(FishData fish) {
        List<DisplayDimension> defaults = new ArrayList<>(List.of(
                DisplayDimension.NETHER, DisplayDimension.OVERWORLD, DisplayDimension.END));
        if (fish.dimensionList().isEmpty()) {
            return defaults;
        }

        LinkedHashSet<DisplayDimension> listed = new LinkedHashSet<>();
        for (String entry : fish.dimensionList()) {
            DisplayDimension dimension = dimension(entry);
            if (dimension != null) {
                listed.add(dimension);
            }
        }
        if (fish.dimensionListBlacklist()) {
            defaults.removeAll(listed);
            return defaults;
        }
        return listed.isEmpty() ? List.of(DisplayDimension.OVERWORLD) : new ArrayList<>(listed);
    }

    private static List<String> allowedBiomes(FishData fish) {
        if (fish.biomeList().isEmpty()) {
            return List.of("WATER");
        }
        if (!fish.biomeListBlacklist()) {
            return fish.biomeList();
        }

        List<String> biomes = new ArrayList<>(DEFAULT_BIOMES);
        Set<String> excluded = new LinkedHashSet<>();
        for (String entry : fish.biomeList()) {
            excluded.add(biomeLabel(entry));
            excluded.add(biomeVisual(entry));
        }
        biomes.removeIf(entry -> excluded.contains(biomeLabel(entry)) || excluded.contains(biomeVisual(entry)));
        return biomes;
    }

    private static DisplayDimension dimension(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "-1", "nether", "the_nether", "minecraft:the_nether" -> DisplayDimension.NETHER;
            case "0", "overworld", "minecraft:overworld" -> DisplayDimension.OVERWORLD;
            case "1", "end", "the_end", "minecraft:the_end" -> DisplayDimension.END;
            default -> null;
        };
    }

    private static String biomeLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return "UNKNOWN";
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        int colon = value.indexOf(':');
        if (colon >= 0 && colon + 1 < value.length()) {
            value = value.substring(colon + 1);
        }
        value = value.replace('-', '_');
        if (value.contains("MUSHROOM")) return "MUSHROOM";
        if (value.contains("JUNGLE")) return "JUNGLE";
        if (value.contains("SWAMP") || value.contains("MANGROVE")) return "SWAMP";
        if (value.contains("BEACH")) return "BEACH";
        if (value.contains("OCEAN")) return "OCEAN";
        if (value.contains("RIVER")) return "RIVER";
        if (value.contains("MOUNTAIN") || value.contains("HILL") || value.contains("PEAK")) return "MOUNTAIN";
        if (value.contains("FOREST") || value.contains("TAIGA")) return "FOREST";
        if (value.contains("COLD") || value.contains("FROZEN") || value.contains("SNOW") || value.contains("ICE")) return "COLD";
        if (value.contains("DESERT") || value.contains("BADLAND") || value.contains("SAVANNA") || value.contains("SANDY")) return "SANDY";
        if (value.equals("HOT") || value.equals("DRY")) return value;
        if (value.contains("DARK") || value.contains("DEAD") || value.contains("SPOOKY") || value.contains("SOUL")) return "DEAD";
        if (value.contains("PLAINS") || value.contains("MEADOW")) return "PLAINS";
        if (value.contains("NETHER")) return "NETHER";
        if (value.contains("END")) return "END";
        if (value.equals("WATER")) return "WATER";
        return value;
    }

    private static String biomeVisual(String raw) {
        String label = biomeLabel(raw);
        return switch (label) {
            case "MUSHROOM" -> "MUSHROOM";
            case "DEAD", "SPOOKY" -> "DEAD";
            case "JUNGLE" -> "JUNGLE";
            case "SWAMP" -> "SWAMP";
            case "HOT", "DRY", "SANDY" -> "SANDY";
            case "COLD", "SNOWY" -> "COLD";
            case "MOUNTAIN" -> "MOUNTAIN";
            case "FOREST" -> "FOREST";
            case "PLAINS", "RIVER" -> "PLAINS";
            default -> "WATER";
        };
    }

    private static int biomeTexturePosition(String biome) {
        return switch (biome) {
            case "JUNGLE" -> 1;
            case "SANDY" -> 2;
            case "COLD" -> 3;
            case "MOUNTAIN" -> 4;
            case "FOREST" -> 5;
            case "PLAINS" -> 6;
            case "SWAMP" -> 7;
            case "MUSHROOM" -> 8;
            case "DEAD" -> 9;
            default -> 0;
        };
    }

    private static Component biomeName(String label) {
        String known = label.toUpperCase(Locale.ROOT);
        return switch (known) {
            case "MUSHROOM", "DEAD", "SPOOKY", "JUNGLE", "SWAMP", "HOT", "SANDY", "COLD",
                    "MOUNTAIN", "FOREST", "PLAINS", "RIVER", "WATER", "OCEAN", "BEACH", "NETHER",
                    "END", "DRY", "SNOWY", "UNKNOWN" -> Component.translatable("jei.fishingmadebetter.biometype." + known);
            default -> Component.literal(label);
        };
    }

    private static void blit128(GuiGraphics graphics, ResourceLocation texture, int u, int v) {
        graphics.blit(texture, MINIGAME_INNER_X, MINIGAME_INNER_Y, u, v, 128, 24);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(id(path));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }

    private enum DisplayDimension {
        OVERWORLD,
        NETHER,
        END
    }

    private record DisplayView(DisplayDimension dimension, String biomeLabel, String biomeVisual) {
    }
}
