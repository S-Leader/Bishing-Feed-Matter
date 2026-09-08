package net.theawesomegem.fishingmadebetter;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.theawesomegem.fishingmadebetter.client.FmbClientConfig;
import net.theawesomegem.fishingmadebetter.client.FmbClientConfig.HudAnchor;

public final class FishingMadeBetterConfigScreen {
    private FishingMadeBetterConfigScreen() {
    }

    public static Screen create(Screen parent) {
        FmbClientConfig.ClientConfig config = FmbClientConfig.get();
        return YetAnotherConfigLib.createBuilder()
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
                        .build())
                .save(FmbClientConfig::save)
                .build()
                .generateScreen(parent);
    }
}
