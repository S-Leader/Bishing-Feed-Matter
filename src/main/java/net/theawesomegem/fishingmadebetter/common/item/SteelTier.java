package net.theawesomegem.fishingmadebetter.common.item;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public final class SteelTier {
    public static final ToolMaterial INSTANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 750, 7, 2.5F, 14, ItemTags.create(Identifier.fromNamespaceAndPath("c", "ingots/steel")));
}
