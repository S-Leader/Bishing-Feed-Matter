package net.theawesomegem.fishingmadebetter.common.item.fish;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.theawesomegem.fishingmadebetter.common.entity.fish.PinkStarfishProjectile;

public class PinkStarfishItem extends Item {
    public PinkStarfishItem() {
        super(new Properties().rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            PinkStarfishProjectile projectile = new PinkStarfishProjectile(level, player, stack.copyWithCount(1));
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1);
            level.addFreshEntity(projectile);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.getCooldowns().addCooldown(this, 3);
            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
