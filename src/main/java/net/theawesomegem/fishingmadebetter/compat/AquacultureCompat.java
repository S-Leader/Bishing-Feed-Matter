package net.theawesomegem.fishingmadebetter.compat;

import com.teammetallurgy.aquaculture.init.AquaDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class AquacultureCompat {
    public static final Set<Identifier> DISABLED_ITEMS = Set.of(id("bobber"), id("fishing_line"), id("wooden_fillet_knife"), id("stone_fillet_knife"), id("iron_fillet_knife"), id("gold_fillet_knife"), id("diamond_fillet_knife"), id("neptunium_fillet_knife"));

    public static void hideCreativeItems(BuildCreativeModeTabContentsEvent event) {
        var entries = new ArrayList<>(event.getParentEntries());
        entries.addAll(event.getSearchEntries());
        for (ItemStack stack : entries)
            if (DISABLED_ITEMS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem())))
                event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static List<ItemStack> disabledStacks() {
        return DISABLED_ITEMS.stream().map(BuiltInRegistries.ITEM::getValue).filter(Objects::nonNull).map(Item::getDefaultInstance).toList();
    }

    public static Item.Properties rodProperties(Item.Properties properties) {
        if (ModList.get().isLoaded("aquaculture"))
            properties.component(AquaDataComponents.ROD_INVENTORY.get(), ItemContainerContents.EMPTY);
        return properties;
    }

    public static void setFishWeight(ItemStack stack, int weight) {
        if (ModList.get().isLoaded("aquaculture")) {
            stack.set(AquaDataComponents.FISH_WEIGHT.get(), (float) weight);
        }
    }

    public static @Nullable ItemContainerContents rodInventory(ItemStack rod) {
        return ModList.get().isLoaded("aquaculture") ? rod.get(AquaDataComponents.ROD_INVENTORY.get()) : null;
    }

    public static void setRodInventory(ItemStack rod, ItemContainerContents inventory) {
        if (ModList.get().isLoaded("aquaculture")) rod.set(AquaDataComponents.ROD_INVENTORY.get(), inventory);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("aquaculture", path);
    }
}
