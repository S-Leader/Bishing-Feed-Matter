package net.theawesomegem.fishingmadebetter.registry;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.enchantment.HookEnchantment;
import net.theawesomegem.fishingmadebetter.common.enchantment.HookEnchantment.Kind;
import net.theawesomegem.fishingmadebetter.common.item.attachment.HookItem;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Constants.MOD_ID);

    public static final EnchantmentCategory HOOK =
            EnchantmentCategory.create("FISHING_EVOLVED_HOOK",
                    item -> item instanceof HookItem hook && !hook.isCraftingOnly());

    public static final RegistryObject<Enchantment> ABYSSAL_CURSE = register(
            "abyssal_curse", Enchantment.Rarity.VERY_RARE, Kind.ABYSSAL_CURSE, 1, true, true
    );
    public static final RegistryObject<Enchantment> MAGNETIC_HOOK = register(
            "magnetic_grappling_hook", Enchantment.Rarity.UNCOMMON, Kind.MAGNETIC_HOOK, 3, false, false
    );
    public static final RegistryObject<Enchantment> INFINITE_BAIT = register(
            "infinite_bait", Enchantment.Rarity.RARE, Kind.INFINITE_BAIT, 3, false, false
    );
    public static final RegistryObject<Enchantment> TEMPTATION = register(
            "temptation", Enchantment.Rarity.RARE, Kind.TEMPTATION, 3, false, false
    );
    public static final RegistryObject<Enchantment> FRESH_FISH = register(
            "fresh_fish_on_table", Enchantment.Rarity.VERY_RARE, Kind.FRESH_FISH, 1, true, false
    );
    public static final RegistryObject<Enchantment> INFLATED = register(
            "inflated_thickening", Enchantment.Rarity.UNCOMMON, Kind.INFLATED, 1, false, false
    );
    public static final RegistryObject<Enchantment> STRAIGHT_HOOK = register(
            "straight_hook_fishing", Enchantment.Rarity.RARE, Kind.STRAIGHT_HOOK, 5, false, false
    );

    private ModEnchantments() {
    }

    private static RegistryObject<Enchantment> register(
            String id,
            Enchantment.Rarity rarity,
            Kind kind,
            int maxLevel,
            boolean treasureOnly,
            boolean curse
    ) {
        return ENCHANTMENTS.register(id,
                () -> new HookEnchantment(rarity, HOOK, kind, maxLevel, treasureOnly, curse));
    }
}
