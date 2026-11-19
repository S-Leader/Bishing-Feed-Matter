package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KnifeItem extends Item {
    private final String tooltipKey;
    private final KnifeType knifeType;
    private final ToolMaterial tier;

    public KnifeItem(
            ToolMaterial tier, String tooltipKey, KnifeType knifeType, Item.Properties properties) {
        super(properties.durability(tier.durability()).repairable(tier.repairItems()));
        this.tier = tier;
        this.tooltipKey = tooltipKey;
        this.knifeType = knifeType;
    }

    public boolean isFilletKnife() {
        return knifeType == KnifeType.FILLET;
    }

    public boolean isScalingKnife() {
        return knifeType == KnifeType.SCALING;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> output,
            TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        try {
            tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));

        } finally {
            tooltip.forEach(output);
        }
    }

    public enum KnifeType {
        FILLET,
        SCALING
    }
}
