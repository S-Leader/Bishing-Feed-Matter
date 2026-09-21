package net.theawesomegem.fishingmadebetter.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class FmbCommonConfig {
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue WHALE_BREAKS_BLOCKS;
    private static final ForgeConfigSpec.BooleanValue WHALE_DROPS_ENABLED;
    private static final ForgeConfigSpec.IntValue WHALE_SPAWN_CHANCE_PERCENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("whale");
        WHALE_BREAKS_BLOCKS = builder
                .comment(
                        "Whether a charging whale smashes blocks softer than obsidian.",
                        "The mobGriefing game rule and block protection mods are still honoured on top of this switch.",
                        "With breaking disabled the whale is stunned by the wall it rams instead of grinding against it."
                )
                .define("breaksBlocks", true);
        WHALE_DROPS_ENABLED = builder
                .comment(
                        "Whether a slain whale rolls its entity loot table.",
                        "The loot table controls every dropped item and quantity."
                )
                .define("dropsEnabled", true);
        WHALE_SPAWN_CHANCE_PERCENT = builder
                .comment(
                        "Chance that a qualifying submerged cold-ocean shipwreck spawns a whale.",
                        "0 disables shipwreck whale spawning; 100 makes every qualifying wreck pass the roll."
                )
                .defineInRange("spawnChancePercent", 3, 0, 100);
        builder.pop();
        SPEC = builder.build();
    }

    private FmbCommonConfig() {
    }

    public static void register() {
        // These values affect world/gameplay behaviour, so keep them in the world's
        // serverconfig and let Forge sync the loaded SERVER config to clients.
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    public static boolean whaleDropsEnabled() {
        return !SPEC.isLoaded() || WHALE_DROPS_ENABLED.get();
    }

    public static boolean whaleBreaksBlocks() {
        return !SPEC.isLoaded() || WHALE_BREAKS_BLOCKS.get();
    }

    public static int whaleSpawnChancePercent() {
        return SPEC.isLoaded() ? WHALE_SPAWN_CHANCE_PERCENT.get() : 3;
    }

    public static double whaleSpawnChance() {
        return whaleSpawnChancePercent() / 100.0D;
    }

    /**
     * Applies an edit on the logical server and persists the world SERVER config.
     * Never call this from a client-side config screen directly; use the network
     * update request so the server can enforce permissions.
     */
    public static void applyServerEdit(boolean whaleBreaksBlocks, boolean whaleDropsEnabled, int whaleSpawnChancePercent) {
        if (!SPEC.isLoaded()) {
            return;
        }
        WHALE_BREAKS_BLOCKS.set(whaleBreaksBlocks);
        WHALE_DROPS_ENABLED.set(whaleDropsEnabled);
        WHALE_SPAWN_CHANCE_PERCENT.set(Math.max(0, Math.min(100, whaleSpawnChancePercent)));
        SPEC.save();
    }
}
