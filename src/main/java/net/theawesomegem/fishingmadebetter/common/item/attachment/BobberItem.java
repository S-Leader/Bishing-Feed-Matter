package net.theawesomegem.fishingmadebetter.common.item.attachment;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BobberItem extends Item {
    private final boolean lavaBobber;
    private final boolean voidBobber;
    private final int varianceModifier;
    private final int tensioningModifier;
    private final boolean craftingOnly;

    public BobberItem(Properties properties, boolean lavaBobber, boolean voidBobber, int varianceModifier, int tensioningModifier, boolean craftingOnly) {
        super(properties);
        this.lavaBobber = lavaBobber;
        this.voidBobber = voidBobber;
        this.varianceModifier = varianceModifier;
        this.tensioningModifier = tensioningModifier;
        this.craftingOnly = craftingOnly;
    }

    public boolean isLavaBobber() {
        return lavaBobber;
    }

    public boolean isVoidBobber() {
        return voidBobber;
    }

    public int getVarianceModifier() {
        return varianceModifier;
    }

    public int getTensioningModifier() {
        return tensioningModifier;
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
            tooltip.add(Component.translatable("item.fishingmadebetter.bobber_basic.tooltip").withStyle(ChatFormatting.DARK_RED));
            return;
        }
        if (lavaBobber) {
            tooltip.add(Component.translatable("item.fishingmadebetter.bobber_obsidian.tooltip").withStyle(ChatFormatting.GRAY));
        }
        if (voidBobber) {
            tooltip.add(Component.translatable("item.fishingmadebetter.bobber_void.tooltip").withStyle(ChatFormatting.GRAY));
        }
        if (!lavaBobber && !voidBobber) {
            tooltip.add(Component.translatable("item.fishingmadebetter.bobber_water.tooltip").withStyle(ChatFormatting.GRAY));
        }
        if (varianceModifier != 0) {
            tooltip.add(Component.translatable("item.fishingmadebetter.bobber_heavy.tooltip")
                    .append(": +" + varianceModifier)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (tensioningModifier != 0) {
            tooltip.add(Component.translatable("item.fishingmadebetter.bobber_lightweight.tooltip")
                    .append(": +" + tensioningModifier)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
