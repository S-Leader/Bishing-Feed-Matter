package net.theawesomegem.fishingmadebetter.mixins;

import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 暴露 ThrownTrident 受保护的 getPickupItem()。
 * 插进鲸鱼身上的三叉戟要连附魔、命名、耐久一起收好，只能问原实体要这份物品栈。
 */
@Mixin(ThrownTrident.class)
public interface ThrownTridentAccessor {
    @Invoker("getPickupItem")
    ItemStack callGetPickupItem();
}
