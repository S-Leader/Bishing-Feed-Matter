package net.theawesomegem.fishingmadebetter.common.item.attachment;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ReelItem extends Item {
    private final int reelRange;
    private final int reelSpeed;
    private final boolean craftingOnly;

    public ReelItem(Properties properties, int reelRange, int reelSpeed, boolean craftingOnly) {
        super(properties);
        this.reelRange = reelRange;
        this.reelSpeed = reelSpeed;
        this.craftingOnly = craftingOnly;
    }

    public int getReelRange() {
        return reelRange;
    }

    public int getReelSpeed() {
        return reelSpeed;
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
            tooltip.add(Component.translatable("item.fishingmadebetter.reel_basic.tooltip").withStyle(ChatFormatting.DARK_RED));
            return;
        }
        tooltip.add(Component.translatable("tooltip.fishingmadebetter.reel.range")
                .append(": " + reelRange + "m")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.fishingmadebetter.reel.speed")
                .append(": " + reelSpeed + "m/s")
                .withStyle(ChatFormatting.GRAY));
    }
}
