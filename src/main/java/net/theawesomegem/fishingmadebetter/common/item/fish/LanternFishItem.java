package net.theawesomegem.fishingmadebetter.common.item.fish;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.Constants;

public class LanternFishItem extends Item {
    public LanternFishItem(Properties properties) {
        super(
                properties
                        .durability(130)
                        .repairable(Items.IRON_INGOT)
                        .enchantable(5)
                        .food(FishFoods.RAW_FISH)
                        .component(
                                DataComponents.EQUIPPABLE,
                                Equippable.builder(EquipmentSlot.HEAD)
                                        .setAsset(
                                                ResourceKey.create(
                                                        EquipmentAssets.ROOT_ID,
                                                        Identifier.fromNamespaceAndPath(
                                                                Constants.MOD_ID, "lantern_fish")))
                                        .build())
                        .attributes(
                                ItemAttributeModifiers.builder()
                                        .add(
                                                Attributes.ARMOR,
                                                new AttributeModifier(
                                                        Identifier.fromNamespaceAndPath(
                                                                Constants.MOD_ID, "lantern_armor"),
                                                        1,
                                                        AttributeModifier.Operation.ADD_VALUE),
                                                EquipmentSlotGroup.HEAD)
                                        .add(
                                                Attributes.ARMOR_TOUGHNESS,
                                                new AttributeModifier(
                                                        Identifier.fromNamespaceAndPath(
                                                                Constants.MOD_ID,
                                                                "lantern_toughness"),
                                                        1,
                                                        AttributeModifier.Operation.ADD_VALUE),
                                                EquipmentSlotGroup.HEAD)
                                        .add(
                                                Attributes.MAX_HEALTH,
                                                new AttributeModifier(
                                                        Identifier.fromNamespaceAndPath(
                                                                Constants.MOD_ID, "lantern_bite"),
                                                        -.1,
                                                        AttributeModifier.Operation
                                                                .ADD_MULTIPLIED_TOTAL),
                                                EquipmentSlotGroup.HEAD)
                                        .build()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isCrouching())
            return this.components()
                    .get(DataComponents.EQUIPPABLE)
                    .swapWithEquipmentSlot(player.getItemInHand(hand), player);
        if (!player.canEat(false)) return InteractionResult.FAIL;
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }
}
