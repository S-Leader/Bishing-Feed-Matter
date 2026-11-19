package net.theawesomegem.fishingmadebetter.common.item.fish;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.entity.fish.ElectricEelWhip;
import net.theawesomegem.fishingmadebetter.registry.ModEffects;
import net.theawesomegem.fishingmadebetter.registry.ModParticles;
import net.theawesomegem.fishingmadebetter.registry.ModSounds;

public class ElectricEelItem extends TieredItem {
    private final Multimap<Attribute, AttributeModifier> attributes = ImmutableMultimap.of(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 3, AttributeModifier.Operation.ADDITION));

    public ElectricEelItem() {
        super(Tiers.IRON, new Properties().durability(Tiers.IRON.getUses()).rarity(Rarity.EPIC).food(FishFoods.BIG_RAW_FISH));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) return super.use(level, player, hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide && level.getEntitiesOfClass(ElectricEelWhip.class, player.getBoundingBox().inflate(2), whip -> whip.getOwner() == player).isEmpty()) {
            level.addFreshEntity(new ElectricEelWhip(level, player, stack));
            player.getCooldowns().addCooldown(this, 12);
            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(null, player.blockPosition(), ModSounds.EEL_SWING.get(), player.getSoundSource(), 1, 1);
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 160;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide)
            entity.addEffect(new MobEffectInstance(ModEffects.ELECTRIFIED.get(), 600, 3, false, false));
        return result;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level() instanceof ServerLevel level) {
            target.addEffect(new MobEffectInstance(ModEffects.ELECTRIFIED.get(), 200, 0, false, false), attacker);
            for (int i = 0; i < 4; i++) {
                level.sendParticles(ModParticles.SPARK.get(), target.getRandomX(0.5), target.getRandomY(), target.getRandomZ(0.5), 1, 0, 0, 0, 0.1);
            }
        }
        return true;
    }
}
