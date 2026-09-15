package net.theawesomegem.fishingmadebetter.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class FmbCommonConfig {
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue WHALE_BREAKS_BLOCKS;
    private static final ForgeConfigSpec.EnumValue<WhaleDrop> WHALE_DROP;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("blue_whale");
        WHALE_BREAKS_BLOCKS = builder
                .comment(
                        "Whether a charging blue whale smashes blocks softer than obsidian.",
                        "The mobGriefing game rule and block protection mods are still honoured on top of this switch.",
                        "With breaking disabled the whale is stunned by the wall it rams instead of grinding against it."
                )
                .define("breaksBlocks", true);
        WHALE_DROP = builder
                .comment(
                        "What a slain blue whale leaves behind.",
                        "WHALE_STEAK: the mod's own whale steaks (default).",
                        "COD_AND_BONE_MEAL: vanilla cod plus bone meal, for packs without the mod's food chain."
                )
                .defineEnum("drops", WhaleDrop.WHALE_STEAK);
        builder.pop();
        SPEC = builder.build();
    }

    private FmbCommonConfig() {
    }

    public enum WhaleDrop {
        WHALE_STEAK,
        COD_AND_BONE_MEAL
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }

    public static WhaleDrop whaleDrop() {
        return SPEC.isLoaded() ? WHALE_DROP.get() : WhaleDrop.WHALE_STEAK;
    }

    public static boolean whaleBreaksBlocks() {
        // 配置文件尚未加载时（早期加载阶段）退回默认值，避免 ConfigValue 抛 IllegalStateException。
        return !SPEC.isLoaded() || WHALE_BREAKS_BLOCKS.get();
    }
}
