package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BaitBoxItem extends BlockItem {
    public BaitBoxItem(Block block, Properties properties) {
        super(block, properties);
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
            tooltip.add(
                    Component.translatable("block.fishingmadebetter.baitbox.tooltip")
                            .withStyle(ChatFormatting.GRAY));

        } finally {
            tooltip.forEach(output);
        }
    }
}
