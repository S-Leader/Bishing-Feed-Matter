package net.theawesomegem.fishingmadebetter.compat;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public final class AquacultureCompat {
    public static final Set<ResourceLocation> DISABLED_ITEMS = Set.of(
            id("bobber"), id("fishing_line"),
            id("iron_fishing_rod"), id("gold_fishing_rod"), id("diamond_fishing_rod"), id("neptunium_fishing_rod"),
            id("wooden_fillet_knife"), id("stone_fillet_knife"), id("iron_fillet_knife"),
            id("gold_fillet_knife"), id("diamond_fillet_knife"), id("neptunium_fillet_knife"),
            id("iron_hook"), id("gold_hook"), id("diamond_hook"), id("light_hook"), id("heavy_hook"),
            id("double_hook"), id("redstone_hook"), id("note_hook"), id("nether_star_hook")
    );

    private AquacultureCompat() {
    }

    public static void hideCreativeItems(BuildCreativeModeTabContentsEvent event) {
        Iterator<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>> iterator = event.getEntries().iterator();
        while (iterator.hasNext()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(iterator.next().getKey().getItem());
            if (DISABLED_ITEMS.contains(id)) {
                iterator.remove();
            }
        }
    }

    public static List<ItemStack> disabledStacks() {
        return DISABLED_ITEMS.stream()
                .map(BuiltInRegistries.ITEM::get)
                .filter(item -> item != null && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("aquaculture"))
                .map(Item::getDefaultInstance)
                .collect(Collectors.toList());
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("aquaculture", path);
    }
}
