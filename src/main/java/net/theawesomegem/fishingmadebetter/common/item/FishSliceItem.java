package net.theawesomegem.fishingmadebetter.common.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class FishSliceItem extends Item {
    public FishSliceItem(int nutrition, float saturationModifier) {
        super(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturationModifier)
                .build()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("FishDisplayName")) {
            tooltip.add(Component.translatable("item.fishingmadebetter.fish_slice_raw.tooltip")
                    .append(" ")
                    .append(tag.getString("FishDisplayName"))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public ItemStack createStack(String itemId, @Nullable String displayName, int amount) {
        ItemStack stack = new ItemStack(this, amount);
        CompoundTag tag = new CompoundTag();
        tag.putString("FishItemId", itemId);
        if (displayName != null) {
            tag.putString("FishDisplayName", displayName);
        }
        stack.setTag(tag);
        return stack;
    }
}
