package net.theawesomegem.fishingmadebetter.common.item.fish;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.Constants;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectricEelWhip;
import net.theawesomegem.fishingmadebetter.registry.ModEffects;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;
import net.theawesomegem.fishingmadebetter.registry.ModSounds;

public class ElectricEelItem extends Item {
    public ElectricEelItem(Properties properties) {
        super(
                properties
                        .durability(ToolMaterial.IRON.durability())
                        .repairable(ToolMaterial.IRON.repairItems())
                        .enchantable(ToolMaterial.IRON.enchantmentValue())
                        .rarity(Rarity.EPIC)
                        .food(FishFoods.BIG_RAW_FISH)
                        .attributes(
                                ItemAttributeModifiers.builder()
                                        .add(
                                                Attributes.ATTACK_DAMAGE,
                                                new AttributeModifier(
                                                        Identifier.fromNamespaceAndPath(
                                                                Constants.MOD_ID, "eel_attack"),
                                                        3,
                                                        AttributeModifier.Operation.ADD_VALUE),
                                                EquipmentSlotGroup.MAINHAND)
                                        .build()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) return super.use(level, player, hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level.isClientSide()
                && level.getEntitiesOfClass(
                        ElectricEelWhip.class,
                        player.getBoundingBox().inflate(2),
                        whip -> whip.getOwner() == player)
                .isEmpty()) {
            level.addFreshEntity(new ElectricEelWhip(level, player, stack));
            player.getCooldowns().addCooldown(stack, 12);
            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(
                    null,
                    player.blockPosition(),
                    ModSounds.EEL_SWING.get(),
                    player.getSoundSource(),
                    1,
                    1);
            stack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 160;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide())
            entity.addEffect(new MobEffectInstance(ModEffects.ELECTRIFIED, 600, 3, false, false));
        return result;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level() instanceof ServerLevel level) {
            target.addEffect(
                    new MobEffectInstance(ModEffects.ELECTRIFIED, 200, 0, false, false), attacker);
            for (int i = 0; i < 4; i++)
                level.sendParticles(
                        ModParticles.SPARK.get(),
                        target.getRandomX(.5),
                        target.getRandomY(),
                        target.getRandomZ(.5),
                        1,
                        0,
                        0,
                        0,
                        .1);
        }
    }
}
