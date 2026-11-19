package net.theawesomegem.fishingmadebetter.common.item.fish;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PufferfishBomb;
import net.theawesomegem.fishingmadebetter.common.entity.fish.WaterIgnoringDamageCalculator;
import net.theawesomegem.fishingmadebetter.registry.ModEntities;

public class PufferfishBombItem extends Item {
    public PufferfishBombItem() {
        super(new Properties().stacksTo(16).rarity(Rarity.RARE).food(FishFoods.BIG_RAW_FISH));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) return super.use(level, player, hand);
        if (!level.isClientSide) {
            PufferfishBomb bomb = ModEntities.PUFFERFISH_BOMB.get().create(level);
            if (bomb == null) return InteractionResultHolder.fail(stack);
            bomb.setPos(player.getEyePosition().add(player.getLookAngle().scale(0.5)));
            bomb.setYRot(player.getYRot());
            bomb.setDeltaMovement(player.getLookAngle().scale(1.5));
            bomb.setPuffState(PufferfishBomb.STATE_BOOM);
            bomb.setInflateCounter(PufferfishBomb.TOTAL_TIME_TO_BOOM - PufferfishBomb.BOOM_DELAY + 1);
            bomb.setFromBucket(true);
            level.addFreshEntity(bomb);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.getCooldowns().addCooldown(this, 10);
            level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1, 1);
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
        if (!level.isClientSide) {
            level.explode(null, level.damageSources().explosion(null, entity), new WaterIgnoringDamageCalculator(), entity.getX(), entity.getY(), entity.getZ(), 3, false, Level.ExplosionInteraction.NONE);
        }
        return result;
    }
}
