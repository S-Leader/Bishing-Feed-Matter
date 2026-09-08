package net.theawesomegem.fishingmadebetter.common.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class KnifeItem extends Item {
    private final String tooltipKey;
    private final KnifeType knifeType;
    private final Tier tier;

    public KnifeItem(Tier tier, String tooltipKey, KnifeType knifeType) {
        this(tier, tooltipKey, knifeType, false);
    }

    public KnifeItem(Tier tier, String tooltipKey, KnifeType knifeType, boolean fireResistant) {
        super(properties(tier, fireResistant));
        this.tier = tier;
        this.tooltipKey = tooltipKey;
        this.knifeType = knifeType;
    }

    private static Item.Properties properties(Tier tier, boolean fireResistant) {
        Item.Properties properties = new Item.Properties().durability(tier.getUses());
        return fireResistant ? properties.fireResistant() : properties;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return tier.getRepairIngredient().test(repairCandidate) || super.isValidRepairItem(stack, repairCandidate);
    }

    public boolean isFilletKnife() {
        return knifeType == KnifeType.FILLET;
    }

    public boolean isScalingKnife() {
        return knifeType == KnifeType.SCALING;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
    }

    public enum KnifeType {
        FILLET,
        SCALING
    }
}
