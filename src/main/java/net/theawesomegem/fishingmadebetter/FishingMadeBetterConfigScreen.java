package net.theawesomegem.fishingmadebetter;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.theawesomegem.fishingmadebetter.client.FmbClientConfig;
import net.theawesomegem.fishingmadebetter.client.FmbClientConfig.HudAnchor;
import net.theawesomegem.fishingmadebetter.client.ServerConfigClientState;

public final class FishingMadeBetterConfigScreen {
    private FishingMadeBetterConfigScreen() {
    }

    public static Screen create(Screen parent) {
        FmbClientConfig.ClientConfig config = FmbClientConfig.get();
        ServerDraft serverDraft = new ServerDraft(
                ServerConfigClientState.whaleBreaksBlocks(),
                ServerConfigClientState.whaleDropsEnabled(),
                ServerConfigClientState.whaleSpawnChancePercent()
        );

        var builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.fishingmadebetter.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.fishingmadebetter.category.hud"))
                        .option(Option.<HudAnchor>createBuilder()
                                .name(Component.translatable("config.fishingmadebetter.hud_anchor"))
                                .description(OptionDescription.of(Component.translatable("config.fishingmadebetter.hud_anchor.description")))
                                .binding(HudAnchor.TOP_CENTER, () -> config.hudAnchor, value -> config.hudAnchor = value)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(HudAnchor.class)
                                        .formatValue(value -> Component.translatable("config.fishingmadebetter.hud_anchor." + value.name().toLowerCase())))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("config.fishingmadebetter.hud_offset_x"))
                                .description(OptionDescription.of(Component.translatable("config.fishingmadebetter.hud_offset_x.description")))
                                .binding(0, () -> config.hudOffsetX, value -> config.hudOffsetX = value)
                                .controller(option -> IntegerSliderControllerBuilder.create(option).range(-500, 500).step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("config.fishingmadebetter.hud_offset_y"))
                                .description(OptionDescription.of(Component.translatable("config.fishingmadebetter.hud_offset_y.description")))
                                .binding(4, () -> config.hudOffsetY, value -> config.hudOffsetY = value)
                                .controller(option -> IntegerSliderControllerBuilder.create(option).range(-300, 300).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.fishingmadebetter.show_hud_distance"))
                                .description(OptionDescription.of(Component.translatable("config.fishingmadebetter.show_hud_distance.description")))
                                .binding(true, () -> config.showHudDistance, value -> config.showHudDistance = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build());

        // SERVER config is editable only after the logical server has explicitly told
        // this client that it has permission. This avoids changing a client-side copy
        // of a Forge SERVER config and pretending it changed the dedicated server.
        if (Minecraft.getInstance().getConnection() != null && ServerConfigClientState.canEdit()) {
            builder = builder.category(ConfigCategory.createBuilder()
                    .name(Component.translatable("config.fishingmadebetter.category.server"))
                    .option(Option.<Boolean>createBuilder()
                            .name(Component.translatable("config.fishingmadebetter.whale_breaks_blocks"))
                            .description(OptionDescription.of(Component.translatable("config.fishingmadebetter.whale_breaks_blocks.description")))
                            .binding(true, () -> serverDraft.whaleBreaksBlocks, value -> serverDraft.whaleBreaksBlocks = value)
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                    .option(Option.<Boolean>createBuilder()
                            .name(Component.translatable("config.fishingmadebetter.whale_drops_enabled"))
                            .description(OptionDescription.of(Component.translatable("config.fishingmadebetter.whale_drops_enabled.description")))
                            .binding(true, () -> serverDraft.whaleDropsEnabled, value -> serverDraft.whaleDropsEnabled = value)
                            .controller(TickBoxControllerBuilder::create)
                            .build())
                    .option(Option.<Integer>createBuilder()
                            .name(Component.translatable("config.fishingmadebetter.whale_spawn_chance"))
                            .description(OptionDescription.of(Component.translatable("config.fishingmadebetter.whale_spawn_chance.description")))
                            .binding(3, () -> serverDraft.whaleSpawnChancePercent, value -> serverDraft.whaleSpawnChancePercent = value)
                            .controller(option -> IntegerSliderControllerBuilder.create(option).range(0, 100).step(1))
                            .build())
                    .build());
        }

        return builder
                .save(() -> {
                    FmbClientConfig.save();
                    if (Minecraft.getInstance().getConnection() != null && ServerConfigClientState.canEdit()) {
                        FishingMadeBetterForge.sendServerConfigUpdate(serverDraft.whaleBreaksBlocks, serverDraft.whaleDropsEnabled, serverDraft.whaleSpawnChancePercent);
                    }
                })
                .build()
                .generateScreen(parent);
    }

    private static final class ServerDraft {
        private boolean whaleBreaksBlocks;
        private boolean whaleDropsEnabled;
        private int whaleSpawnChancePercent;

        private ServerDraft(boolean whaleBreaksBlocks, boolean whaleDropsEnabled, int whaleSpawnChancePercent) {
            this.whaleBreaksBlocks = whaleBreaksBlocks;
            this.whaleDropsEnabled = whaleDropsEnabled;
            this.whaleSpawnChancePercent = whaleSpawnChancePercent;
        }
    }
}
