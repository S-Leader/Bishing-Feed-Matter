package net.theawesomegem.fishingmadebetter.common.item.attachment;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class HookItem extends Item {
    private final int tuggingReduction;
    private final int treasureModifier;
    private final int biteRateModifier;
    private final int weightModifier;
    private final boolean craftingOnly;

    public HookItem(Properties properties, int tuggingReduction, int treasureModifier, int biteRateModifier, int weightModifier, boolean craftingOnly) {
        super(properties);
        this.tuggingReduction = tuggingReduction;
        this.treasureModifier = treasureModifier;
        this.biteRateModifier = biteRateModifier;
        this.weightModifier = weightModifier;
        this.craftingOnly = craftingOnly;
    }

    public int getTuggingReduction() {
        return tuggingReduction;
    }

    public int getTreasureModifier() {
        return treasureModifier;
    }

    public int getBiteRateModifier() {
        return biteRateModifier;
    }

    public int getWeightModifier() {
        return weightModifier;
    }

    public boolean isCraftingOnly() {
        return craftingOnly;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (craftingOnly) {
            tooltip.add(Component.translatable("item.fishingmadebetter.hook_basic.tooltip").withStyle(ChatFormatting.DARK_RED));
            return;
        }
        if (tuggingReduction != 0) {
            tooltip.add(Component.translatable("item.fishingmadebetter.hook_barbed.tooltip")
                    .append(": " + tuggingReduction)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (treasureModifier != 0) {
            tooltip.add(Component.translatable("item.fishingmadebetter.hook_magnetic.tooltip")
                    .append(": +" + treasureModifier + "%")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (biteRateModifier != 0) {
            tooltip.add(Component.translatable("item.fishingmadebetter.hook_shiny.tooltip")
                    .append(": +" + biteRateModifier + "%")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (weightModifier != 0) {
            tooltip.add(Component.translatable("item.fishingmadebetter.hook_fatty.tooltip")
                    .append(": +" + weightModifier + "%")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
