package net.theawesomegem.fishingmadebetter.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Enchantment shared by every Fishing Evolved hook enchantment.
 */
public final class HookEnchantment extends Enchantment {
    private final Kind kind;
    private final int maxLevel;
    private final boolean treasureOnly;
    private final boolean curse;

    public HookEnchantment(
            Rarity rarity,
            EnchantmentCategory category,
            Kind kind,
            int maxLevel,
            boolean treasureOnly,
            boolean curse
    ) {
        super(rarity, category, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
        this.kind = kind;
        this.maxLevel = maxLevel;
        this.treasureOnly = treasureOnly;
        this.curse = curse;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getMinCost(int level) {
        int base = switch (kind) {
            case ABYSSAL_CURSE, FRESH_FISH -> 25;
            case TEMPTATION -> 18;
            case INFINITE_BAIT, STRAIGHT_HOOK -> 14;
            case MAGNETIC_HOOK -> 10;
            case INFLATED -> 8;
        };
        return base + Math.max(0, level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return treasureOnly;
    }

    @Override
    public boolean isCurse() {
        return curse;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        if (!super.checkCompatibility(other)) {
            return false;
        }
        if (kind == Kind.TEMPTATION) {
            return other == Enchantments.UNBREAKING;
        }
        if (!(other instanceof HookEnchantment hook)) {
            return true;
        }

        Kind otherKind = hook.kind;
        if (otherKind == Kind.TEMPTATION) {
            return false;
        }
        if (isPair(kind, otherKind, Kind.MAGNETIC_HOOK, Kind.INFINITE_BAIT)) {
            return false;
        }
        if (isPair(kind, otherKind, Kind.ABYSSAL_CURSE, Kind.FRESH_FISH)) {
            return false;
        }
        return !isPair(kind, otherKind, Kind.STRAIGHT_HOOK, Kind.ABYSSAL_CURSE)
                && !isPair(kind, otherKind, Kind.STRAIGHT_HOOK, Kind.FRESH_FISH);
    }

    private static boolean isPair(Kind first, Kind second, Kind left, Kind right) {
        return first == left && second == right || first == right && second == left;
    }

    public enum Kind {
        ABYSSAL_CURSE,
        MAGNETIC_HOOK,
        INFINITE_BAIT,
        TEMPTATION,
        FRESH_FISH,
        INFLATED,
        STRAIGHT_HOOK
    }
}
