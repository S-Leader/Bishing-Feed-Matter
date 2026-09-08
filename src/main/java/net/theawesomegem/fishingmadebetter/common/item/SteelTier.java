package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum SteelTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 750;
    }

    @Override
    public float getSpeed() {
        return 7.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2.5F;
    }

    @Override
    public int getLevel() {
        return 2;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemTags.create(new ResourceLocation("forge", "ingots/steel")));
    }
}
