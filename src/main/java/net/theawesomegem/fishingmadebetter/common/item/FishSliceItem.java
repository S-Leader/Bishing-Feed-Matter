package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.theawesomegem.fishingmadebetter.common.util.StackData;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FishSliceItem extends Item {
    public FishSliceItem(int nutrition, float saturationModifier, Item.Properties properties) {
        super(
                properties.food(
                        new FoodProperties.Builder()
                                .nutrition(nutrition)
                                .saturationModifier(saturationModifier)
                                .build()));
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
            CompoundTag tag = StackData.get(stack);
            if (tag != null && tag.contains("FishDisplayName")) {
                tooltip.add(
                        Component.translatable("item.fishingmadebetter.fish_slice_raw.tooltip")
                                .append(" ")
                                .append(tag.getStringOr("FishDisplayName", ""))
                                .withStyle(ChatFormatting.GRAY));
            }

        } finally {
            tooltip.forEach(output);
        }
    }

    public ItemStack createStack(String itemId, @Nullable String displayName, int amount) {
        ItemStack stack = new ItemStack(this, amount);
        CompoundTag tag = new CompoundTag();
        tag.putString("FishItemId", itemId);
        if (displayName != null) {
            tag.putString("FishDisplayName", displayName);
        }
        StackData.set(stack, tag);
        return stack;
    }
}
